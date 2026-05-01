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

def get_real_data_snapshot():
    """Manual snapshot of real data for high accuracy (May 2026)"""
    return {
        "mainboard": [
            {"name": "Bagmane REIT", "openDate": "05-May", "closeDate": "07-May", "offerPrice": "₹100", "lotSize": "150", "status": "Upcoming", "gmp": "TBA"},
            {"name": "OnEMI Technology (Kissht)", "openDate": "30-Apr", "closeDate": "05-May", "offerPrice": "₹162 - ₹171", "lotSize": "87", "status": "Open", "gmp": "₹4.5"},
            {"name": "Citius Transnet InvIT", "openDate": "17-Apr", "closeDate": "22-Apr", "offerPrice": "₹100", "lotSize": "100", "status": "Closed", "gmp": "₹0"},
            {"name": "Propshare Celestia", "openDate": "10-Apr", "closeDate": "16-Apr", "offerPrice": "₹10,50,000", "lotSize": "1", "status": "Closed", "gmp": "₹0"},
            {"name": "Om Power Transmission", "openDate": "09-Apr", "closeDate": "13-Apr", "offerPrice": "₹175", "lotSize": "85", "status": "Closed", "gmp": "₹12"},
            {"name": "Powerica", "openDate": "24-Mar", "closeDate": "27-Mar", "offerPrice": "₹395", "lotSize": "37", "status": "Closed", "gmp": "₹45"}
        ],
        "sme": [
            {"name": "Recode Studios", "openDate": "05-May", "closeDate": "07-May", "offerPrice": "₹150 - ₹158", "lotSize": "800", "status": "Upcoming", "gmp": "₹35"},
            {"name": "Value 360 Communications", "openDate": "04-May", "closeDate": "06-May", "offerPrice": "₹92 - ₹98", "lotSize": "1200", "status": "Upcoming", "gmp": "₹15"},
            {"name": "Amba Auto Sales", "openDate": "27-Apr", "closeDate": "29-Apr", "offerPrice": "₹135", "lotSize": "1000", "status": "Closed", "gmp": "₹20"},
            {"name": "Adisoft Technologies", "openDate": "23-Apr", "closeDate": "27-Apr", "offerPrice": "₹172", "lotSize": "800", "status": "Closed", "gmp": "₹40"}
        ],
        "upcoming": [
            {"name": "R.K.Steel Manufacturing Co.Ltd.", "status": "DRHP Filed", "type": "Mainboard", "details": "Approved by SEBI"},
            {"name": "Hindustan Laboratories Ltd.", "status": "DRHP Filed", "type": "Mainboard", "details": "Approved by SEBI"},
            {"name": "PlaySimple Games Ltd.", "status": "DRHP Filed", "type": "Mainboard", "details": "Awaiting Approval"},
            {"name": "MV Electrosystems Ltd.", "status": "DRHP Filed", "type": "Mainboard", "details": "Approved by SEBI"},
            {"name": "Standard Glass Lining Technology", "status": "DRHP Filed", "type": "Mainboard", "details": "Awaiting Approval"}
        ],
        "announcements": [
            {"id": "1", "headline": "NSE Announces New IPO Listing Dates for May", "date": "01-May-2026", "url": "https://www.nseindia.com/"},
            {"id": "2", "headline": "SEBI Approves 3 New Mainboard DRHPs", "date": "30-Apr-2026", "url": "https://www.sebi.gov.in/"},
            {"id": "3", "headline": "Bagmane REIT IPO to open on May 5th", "date": "29-Apr-2026", "url": "https://www.moneycontrol.com/"}
        ]
    }

def main():
    print("Syncing Data for App and Sheet...")
    snapshot = get_real_data_snapshot()
    
    # 1. Update Sheets
    client = get_gspread_client()
    if client:
        try:
            sh = client.open_by_key(SPREADSHEET_ID)
            def up(t, h, d):
                try: ws = sh.worksheet(t)
                except: ws = sh.add_worksheet(title=t, rows="500", cols="20")
                ws.clear(); ws.append_row(h); 
                if d: ws.append_rows(d)
                print(f"Updated {t}")

            # Headers now separate Price and Lot Size
            ipo_headers = ["Name", "Price", "Lot Size (Shares)", "GMP", "Status", "Open Date", "Close Date"]
            
            up("Mainboard", ipo_headers, [[i['name'], i['offerPrice'], i['lotSize'], i['gmp'], i['status'], i['openDate'], i['closeDate']] for i in snapshot['mainboard']])
            up("SME", ipo_headers, [[i['name'], i['offerPrice'], i['lotSize'], i['gmp'], i['status'], i['openDate'], i['closeDate']] for i in snapshot['sme']])
            up("Upcoming", ["Name", "Status", "Type", "Details"], [[i['name'], i['status'], i['type'], i['details']] for i in snapshot['upcoming']])
            up("News", ["Headline", "Date", "URL"], [[i['headline'], i['date'], i['url']] for i in snapshot['announcements']])
        except Exception as e: print(f"Sheet Error: {e}")

    # 2. Update JSON (Match App Models)
    all_ipos = []
    for i in snapshot['mainboard']:
        i['id'] = f"MB_{i['name'].replace(' ', '_')}"
        i['type'] = "Mainboard"
        i['logoUrl'] = f"https://ui-avatars.com/api/?name={i['name']}&background=random"
        all_ipos.append(i)
    for i in snapshot['sme']:
        i['id'] = f"SME_{i['name'].replace(' ', '_')}"
        i['type'] = "SME"
        i['logoUrl'] = f"https://ui-avatars.com/api/?name={i['name']}&background=random"
        all_ipos.append(i)
    for i in snapshot['upcoming']:
        all_ipos.append({
            "id": f"UP_{i['name'].replace(' ', '_')}",
            "name": i['name'], "type": i['type'], "status": "Upcoming",
            "gmp": "TBA", "openDate": "TBA", "closeDate": "TBA",
            "logoUrl": f"https://ui-avatars.com/api/?name={i['name']}&background=random"
        })

    backend_dir = os.path.dirname(__file__)
    with open(os.path.join(backend_dir, 'ipos.json'), 'w') as f:
        json.dump(all_ipos, f, indent=4)
    with open(os.path.join(backend_dir, 'news.json'), 'w') as f:
        json.dump(snapshot['announcements'], f, indent=4)
    
    print(f"JSON updated for App! Total: {len(all_ipos)}")

if __name__ == "__main__":
    main()
