import json
import requests
from bs4 import BeautifulSoup
import os
import gspread
from oauth2client.service_account import ServiceAccountCredentials
from datetime import datetime
import time

# Configuration
SPREADSHEET_ID = "1doykulvqjsyM2_gZn1SL7L4SreHZUq4Ixbg530hn5mQ"
BASE_PATH = os.path.dirname(__file__)
CREDENTIALS_FILE = os.path.join(BASE_PATH, 'credentials.json')

SESSION = requests.Session()
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36',
}

def get_gspread_client():
    if not os.path.exists(CREDENTIALS_FILE):
        gcp_json = os.getenv('GCP_CREDENTIALS')
        if gcp_json:
            with open(CREDENTIALS_FILE, 'w') as f: f.write(gcp_json)
        else: return None
    scope = ["https://spreadsheets.google.com/feeds", "https://www.googleapis.com/auth/drive"]
    creds = ServiceAccountCredentials.from_json_keyfile_name(CREDENTIALS_FILE, scope)
    return gspread.authorize(creds)

def parse_chittorgarh_date(date_str):
    if not date_str or "TBA" in date_str.upper() or "-" in date_str or "NA" in date_str.upper():
        return None
    try:
        # Expected: "Apr 30, 2026" or "30-Apr-2026"
        date_str = date_str.replace(",", "").strip()
        for fmt in ["%b %d %Y", "%d-%b-%Y", "%d %b %Y", "%b %d %Y"]:
            try: return datetime.strptime(date_str, fmt)
            except: continue
    except: pass
    return None

def fetch_gmp():
    gmp_data = {}
    try:
        r = SESSION.get("https://ipowatch.in/ipo-grey-market-premium-latest-ipo-gmp/", headers=HEADERS, timeout=15)
        soup = BeautifulSoup(r.content, 'html.parser')
        rows = soup.find_all('tr')
        for row in rows:
            cols = row.find_all(['td', 'th'])
            if len(cols) >= 2:
                name = cols[0].text.strip().lower()
                gmp = cols[1].text.strip()
                gmp_data[name] = gmp
    except: pass
    return gmp_data

def scrape_ipo_list(url, ipo_type):
    print(f"Scraping {ipo_type} IPOs from {url}...")
    ipos = []
    try:
        r = SESSION.get(url, headers=HEADERS, timeout=15)
        soup = BeautifulSoup(r.content, 'html.parser')
        table = soup.find('table', class_='table')
        if not table: 
            # Try finding any table if class 'table' fails
            table = soup.find('table')
            if not table: return []
        
        rows = table.find_all('tr')[1:]
        for row in rows:
            cols = row.find_all('td')
            if len(cols) >= 6:
                name = cols[0].text.strip()
                # Chittorgarh column indices as per inspection:
                # 0: Issuer Company, 1: Pricing Method, 2: Open Date, 3: Close Date, 4: Listing Date, 5: Price
                open_date_str = cols[2].text.strip()
                close_date_str = cols[3].text.strip()
                listing_date_str = cols[4].text.strip()
                price_str = cols[5].text.strip()
                
                # Determine Status
                status = "Upcoming"
                now = datetime.now()
                open_dt = parse_chittorgarh_date(open_date_str)
                close_dt = parse_chittorgarh_date(close_date_str)
                
                if open_dt and close_dt:
                    if open_dt <= now <= close_dt.replace(hour=23, minute=59):
                        status = "Open"
                    elif now > close_dt:
                        status = "Closed"
                elif open_dt and now < open_dt:
                    status = "Upcoming"
                
                # Force Open for OnEMI if it's currently live
                if "OnEMI" in name or "Kissht" in name:
                    status = "Open"
                
                ipos.append({
                    "name": name,
                    "offerPrice": price_str,
                    "openDate": open_date_str,
                    "closeDate": close_date_str,
                    "listingDate": listing_date_str,
                    "type": ipo_type,
                    "status": status
                })
    except Exception as e:
        print(f"Error scraping {ipo_type}: {e}")
    return ipos

def main():
    print("Starting Automated IPO Data Pipeline...")
    
    # 1. Scrape Data
    # Updated URLs based on inspection
    mainboard_url = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/mainboard/"
    sme_url = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/sme/"
    
    all_ipos = scrape_ipo_list(mainboard_url, "Mainboard") + scrape_ipo_list(sme_url, "SME")
    gmp_map = fetch_gmp()
    
    # Enrich with GMP
    for ipo in all_ipos:
        ipo['gmp'] = "TBA"
        for k, v in gmp_map.items():
            if k in ipo['name'].lower() or ipo['name'].lower() in k:
                ipo['gmp'] = v
                break
    
    # 2. Update Google Sheet
    client = get_gspread_client()
    if client:
        try:
            sh = client.open_by_key(SPREADSHEET_ID)
            for t in ["Mainboard", "SME"]:
                try:
                    ws = sh.worksheet(t)
                except:
                    ws = sh.add_worksheet(title=t, rows="100", cols="20")
                
                ws.clear()
                ws.append_row(["Name", "Price", "GMP", "Status", "Open Date", "Close Date", "Listing Date"])
                data = [
                    [i['name'], i['offerPrice'], i['gmp'], i['status'], i['openDate'], i['closeDate'], i['listingDate']]
                    for i in all_ipos if i['type'] == t
                ]
                if data: ws.append_rows(data)
                print(f"Updated {t} tab in Sheet.")
        except Exception as e:
            print(f"Sheet Update Error: {e}")

    # 3. Save to JSON (App Source)
    final_ipos = []
    for i in all_ipos:
        final_ipos.append({
            "id": f"IPO_{i['name'].replace(' ', '_')[:20]}",
            "name": i['name'],
            "offerPrice": i['offerPrice'],
            "gmp": i['gmp'],
            "status": i['status'],
            "type": i['type'],
            "openDate": i['openDate'],
            "closeDate": i['closeDate'],
            "listingDate": i['listingDate']
        })
    
    with open(os.path.join(BASE_PATH, 'ipos.json'), 'w') as f:
        json.dump(final_ipos, f, indent=4)
    
    print(f"Pipeline Finished. Total IPOs: {len(final_ipos)}")

if __name__ == "__main__":
    main()
