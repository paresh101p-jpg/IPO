import json
import requests
from bs4 import BeautifulSoup
import os
import gspread
from oauth2client.service_account import ServiceAccountCredentials
from datetime import datetime
import time
import re

# Configuration
SPREADSHEET_ID = "1doykulvqjsyM2_gZn1SL7L4SreHZUq4Ixbg530hn5mQ"
BASE_PATH = os.path.dirname(__file__)
CREDENTIALS_FILE = os.path.join(BASE_PATH, 'credentials.json')
INDIAN_API_KEY = os.getenv('INDIAN_API_KEY') # Should be set in GitHub Secrets

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

def get_logo_url(name):
    clean_name = re.sub(r'[^a-zA-Z0-9]', '', name.split()[0].lower())
    # Try clearbit logo but with a fallback to UI avatars
    return f"https://ui-avatars.com/api/?name={name}&background=random&color=fff&size=128"

def fetch_indian_api(endpoint):
    """Fetch from IndianAPI if key is available."""
    if not INDIAN_API_KEY: return []
    url = f"https://stock.indianapi.in/{endpoint}"
    try:
        r = SESSION.get(url, headers={"x-api-key": INDIAN_API_KEY}, timeout=15)
        if r.status_code == 200:
            return r.json().get('data', r.json())
    except: pass
    return []

def scrape_chittorgarh_ipos(ipo_type="mainboard"):
    """Fallback scraper for Chittorgarh."""
    url = f"https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/{ipo_type}/"
    ipos = []
    try:
        r = SESSION.get(url, headers=HEADERS, timeout=15)
        soup = BeautifulSoup(r.content, 'html.parser')
        table = soup.find('table')
        if not table: return []
        rows = table.find_all('tr')[1:]
        for row in rows:
            cols = row.find_all('td')
            if len(cols) >= 6:
                name = cols[0].text.strip()
                ipos.append({
                    "name": name,
                    "openDate": cols[2].text.strip(),
                    "closeDate": cols[3].text.strip(),
                    "listingDate": cols[4].text.strip(),
                    "offerPrice": cols[5].text.strip(),
                    "status": "Upcoming" # Default, will be inferred
                })
    except: pass
    return ipos

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

def infer_status(open_date_str, close_date_str):
    now = datetime.now()
    try:
        # Simple date parser
        for fmt in ["%d-%b-%Y", "%b %d, %Y", "%d %b %Y"]:
            try:
                open_dt = datetime.strptime(open_date_str.replace(",", "").strip(), fmt)
                close_dt = datetime.strptime(close_date_str.replace(",", "").strip(), fmt)
                if open_dt <= now <= close_dt.replace(hour=23, minute=59):
                    return "Open"
                elif now > close_dt:
                    return "Closed"
                else:
                    return "Upcoming"
            except: continue
    except: pass
    return "Upcoming"

def main():
    print("Starting Master IPO + Buyback Data Ecosystem...")
    
    # 1. Fetch IPO Data
    api_ipos = fetch_indian_api("ipo")
    cg_main = scrape_chittorgarh_ipos("mainboard")
    cg_sme = scrape_chittorgarh_ipos("sme")
    gmp_map = fetch_gmp()
    
    merged_ipos = []
    seen = set()
    
    # Process API IPOs
    for ipo in api_ipos:
        name = ipo.get('company_name', ipo.get('name', 'Unknown'))
        if name in seen: continue
        seen.add(name)
        
        status = ipo.get('status', 'Upcoming')
        if status.lower() == 'active': status = 'Open'
        
        merged_ipos.append({
            "id": f"IPO_{name.replace(' ', '_')[:20]}",
            "name": name,
            "offerPrice": ipo.get('price_band', ipo.get('price', 'TBA')),
            "gmp": gmp_map.get(name.lower(), "TBA"),
            "status": status,
            "type": "SME" if "SME" in name else "Mainboard",
            "openDate": ipo.get('open_date', 'TBA'),
            "closeDate": ipo.get('close_date', 'TBA'),
            "listingDate": ipo.get('listing_date', 'TBA'),
            "logoUrl": get_logo_url(name)
        })
    
    # Process Fallback IPOs
    for ipo in (cg_main + cg_sme):
        if ipo['name'] in seen: continue
        seen.add(ipo['name'])
        status = infer_status(ipo['openDate'], ipo['closeDate'])
        merged_ipos.append({
            "id": f"IPO_{ipo['name'].replace(' ', '_')[:20]}",
            "name": ipo['name'],
            "offerPrice": ipo['offerPrice'],
            "gmp": gmp_map.get(ipo['name'].lower(), "TBA"),
            "status": status,
            "type": "Mainboard" if ipo in cg_main else "SME",
            "openDate": ipo['openDate'],
            "closeDate": ipo['closeDate'],
            "listingDate": ipo['listingDate'],
            "logoUrl": get_logo_url(ipo['name'])
        })

    # 2. Fetch Buyback Data
    buybacks = fetch_indian_api("buyback")
    if not buybacks:
        # Mock/Scrape Buyback if API fails (as backup)
        buybacks = [
            {"company_name": "Bajaj Auto Ltd", "price": "₹10,000", "open_date": "06-Mar-2024", "close_date": "13-Mar-2024", "status": "Closed"},
            {"company_name": "TATA Consultancy Services", "price": "₹4,150", "open_date": "01-Dec-2023", "close_date": "07-Dec-2023", "status": "Closed"}
        ]
    
    final_buybacks = []
    for bb in buybacks:
        name = bb.get('company_name', bb.get('name', 'Unknown'))
        final_buybacks.append({
            "id": f"BB_{name.replace(' ', '_')[:20]}",
            "name": name,
            "buybackPrice": bb.get('price', bb.get('buyback_price', 'TBA')),
            "openDate": bb.get('open_date', 'TBA'),
            "closeDate": bb.get('close_date', 'TBA'),
            "status": bb.get('status', 'Upcoming'),
            "logoUrl": get_logo_url(name)
        })

    # 3. Update Google Sheet
    client = get_gspread_client()
    if client:
        try:
            sh = client.open_by_key(SPREADSHEET_ID)
            # Update Mainboard/SME
            for t in ["Mainboard", "SME"]:
                try: ws = sh.worksheet(t)
                except: ws = sh.add_worksheet(title=t, rows="100", cols="20")
                ws.clear()
                ws.append_row(["Name", "Price", "GMP", "Status", "Open Date", "Close Date", "Listing Date"])
                data = [[i['name'], i['offerPrice'], i['gmp'], i['status'], i['openDate'], i['closeDate'], i['listingDate']] for i in merged_ipos if i['type'] == t]
                if data: ws.append_rows(data)

            # Update Buybacks
            try: ws = sh.worksheet("Buybacks")
            except: ws = sh.add_worksheet(title="Buybacks", rows="100", cols="20")
            ws.clear()
            ws.append_row(["Company", "Buyback Price", "Open Date", "Close Date", "Status"])
            data = [[i['name'], i['buybackPrice'], i['openDate'], i['closeDate'], i['status']] for i in final_buybacks]
            if data: ws.append_rows(data)
            
            print("Google Sheets Updated successfully.")
        except Exception as e:
            print(f"Sheet Update Error: {e}")

    # 4. Save to JSON for App
    with open(os.path.join(BASE_PATH, 'ipos.json'), 'w') as f:
        json.dump(merged_ipos, f, indent=4)
    with open(os.path.join(BASE_PATH, 'buybacks.json'), 'w') as f:
        json.dump(final_buybacks, f, indent=4)
    
    print(f"Pipeline Finished. IPOs: {len(merged_ipos)}, Buybacks: {len(final_buybacks)}")

if __name__ == "__main__":
    main()
