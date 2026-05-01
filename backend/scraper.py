import json
import re
import requests
from bs4 import BeautifulSoup
import os
import gspread
from oauth2client.service_account import ServiceAccountCredentials

# Configuration
SPREADSHEET_ID = "1doykulvqjsyM2_gZn1SL7L4SreHZUq4Ixbg530hn5mQ"
INDIAN_API_KEY = os.getenv('INDIAN_API_KEY', 'sk-live-qlmJds6HRpg0A5lEM2yYSF1V5Z99ysuHbsN8Mhlu')
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

def fetch_gmp_map():
    print("Fetching GMP Map from IPOWatch...")
    gmp_map = {}
    try:
        r = SESSION.get("https://ipowatch.in/ipo-grey-market-premium-latest-ipo-gmp/", headers=HEADERS, timeout=15)
        soup = BeautifulSoup(r.content, 'html.parser')
        for row in soup.find_all('tr')[1:]:
            cols = row.find_all(['td', 'th'])
            if len(cols) >= 2:
                name = cols[0].text.strip().lower()
                gmp = cols[1].text.strip()
                gmp_map[name] = gmp
    except: pass
    return gmp_map

def fetch_all_ipos():
    print("Fetching IPOs from IndianAPI + Chittorgarh...")
    ipos = []
    gmp_map = fetch_gmp_map()
    
    # 1. IndianAPI
    try:
        api_url = "https://stock.indianapi.in/ipo"
        r = SESSION.get(api_url, headers={"x-api-key": INDIAN_API_KEY}, timeout=15)
        if r.status_code == 200:
            resp = r.json()
            for s in ["upcoming", "open", "closed"]:
                for item in resp.get(s, []):
                    name = item.get('name', 'Unknown')
                    # Match GMP
                    gmp = "TBA"
                    for k, v in gmp_map.items():
                        if k.split()[0] in name.lower():
                            gmp = f"₹{v}"
                            break
                    
                    ipos.append({
                        "id": f"IPO_{name.replace(' ', '_')[:20]}",
                        "name": name,
                        "price": f"₹{item.get('min_price')} - ₹{item.get('max_price')}" if item.get('min_price') else "TBA",
                        "lotSize": str(item.get('lot_size') or "TBA"),
                        "gmp": gmp,
                        "type": "SME" if item.get('is_sme') else "Mainboard",
                        "status": s.capitalize(),
                        "openDate": item.get('bidding_start_date', 'TBA'),
                        "listingDate": item.get('listing_date', 'TBA')
                    })
    except: pass
    return ipos

def scrape_buybacks_chittorgarh():
    print("Scraping Buybacks from Chittorgarh...")
    buybacks = []
    try:
        r = SESSION.get("https://www.chittorgarh.com/report/latest-buyback-issues-in-india/80/tender-offer-buyback/", headers=HEADERS, timeout=15)
        soup = BeautifulSoup(r.content, 'html.parser')
        table = soup.find('table')
        if table:
            for row in table.find_all('tr')[1:]:
                cols = row.find_all('td')
                if len(cols) >= 5:
                    buybacks.append([cols[0].text.strip(), f"₹{cols[2].text.strip()}", cols[3].text.strip(), cols[4].text.strip(), "Live"])
    except: pass
    return buybacks

def main():
    ipos = fetch_all_ipos()
    buybacks = scrape_buybacks_chittorgarh()
    
    # Update local JSON
    with open(os.path.join(BASE_PATH, 'ipos.json'), 'w') as f: json.dump(ipos, f, indent=4)
    with open(os.path.join(BASE_PATH, 'buybacks.json'), 'w') as f: json.dump([{"name": b[0], "price": b[1], "open": b[2], "close": b[3], "status": b[4]} for b in buybacks], f, indent=4)

    client = get_gspread_client()
    if client:
        try:
            sh = client.open_by_key(SPREADSHEET_ID)
            # IPO Tabs
            for t in ["Mainboard", "SME"]:
                ws = sh.worksheet(t)
                ws.clear()
                ws.append_row(["Name", "Price", "Lot Size", "GMP", "Status", "Open Date", "Listing Date"])
                data = [[i['name'], i['price'], i['lotSize'], i['gmp'], i['status'], i['openDate'], i['listingDate']] for i in ipos if i['type'] == t]
                if data: ws.append_rows(data)
            
            # Buybacks
            ws_bb = sh.worksheet("Buybacks")
            ws_bb.clear()
            ws_bb.append_row(["Company", "Price", "Open", "Close", "Status"])
            if buybacks: ws_bb.append_rows(buybacks)
            
            print("Successfully synced all data with GMP and Buybacks!")
        except Exception as e: print(f"Sync Error: {e}")

def sync_from_sheet():
    print("Syncing from Google Sheet to local JSON...")
    client = get_gspread_client()
    if not client:
        return []

    try:
        sh = client.open_by_key(SPREADSHEET_ID)
        all_ipos = []
        
        # Mainboard & SME
        for t in ["Mainboard", "SME"]:
            try:
                ws = sh.worksheet(t)
                rows = ws.get_all_records()
                for row in rows:
                    all_ipos.append({
                        "id": f"IPO_{row.get('Name', 'Unknown').replace(' ', '_')[:20]}",
                        "name": row.get('Name', 'Unknown'),
                        "offerPrice": row.get('Price', row.get('Offer Price', 'TBA')),
                        "lotSize": str(row.get('Lot Size', 'TBA')),
                        "gmp": row.get('GMP', 'TBA'),
                        "type": t,
                        "status": row.get('Status', 'Upcoming'),
                        "openDate": row.get('Open Date', None),
                        "closeDate": row.get('Close Date', None),
                        "listingDate": row.get('Listing Date', None)
                    })
            except: pass
        
        # Save to JSON
        with open(os.path.join(BASE_PATH, 'ipos.json'), 'w') as f:
            json.dump(all_ipos, f, indent=4)
            
        # Buybacks
        buybacks = []
        try:
            ws_bb = sh.worksheet("Buybacks")
            rows = ws_bb.get_all_records()
            for row in rows:
                buybacks.append({
                    "id": f"BB_{row.get('Company', 'Unknown').replace(' ', '_')[:20]}",
                    "name": row.get('Company', 'Unknown'),
                    "buybackPrice": row.get('Price', 'TBA'),
                    "openDate": row.get('Open', 'TBA'),
                    "closeDate": row.get('Close', 'TBA'),
                    "status": row.get('Status', 'Upcoming')
                })
            with open(os.path.join(BASE_PATH, 'buybacks.json'), 'w') as f:
                json.dump(buybacks, f, indent=4)
        except: pass

        # News
        news = []
        try:
            ws_news = sh.worksheet("News")
            rows = ws_news.get_all_records()
            for i, row in enumerate(rows):
                news.append({
                    "id": str(i + 1),
                    "headline": row.get('Headline', 'Market Update'),
                    "summary": row.get('Summary', ''),
                    "imageUrl": "https://images.unsplash.com/photo-1611974714658-66d2c132042e?auto=format&fit=crop&q=80&w=300",
                    "date": row.get('Date', 'Recently')
                })
            with open(os.path.join(BASE_PATH, 'news.json'), 'w') as f:
                json.dump(news, f, indent=4)
        except: pass

        print(f"Successfully synced {len(all_ipos)} IPOs from Sheet.")
        return all_ipos
    except Exception as e:
        print(f"Sheet Sync Error: {e}")
        return []

def scrape_and_save_ipos():
    # This is the function main.py expects
    return sync_from_sheet()

if __name__ == "__main__":
    sync_from_sheet()
