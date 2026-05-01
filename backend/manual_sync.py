import json
import os
import gspread
from oauth2client.service_account import ServiceAccountCredentials

# Configuration
SPREADSHEET_ID = "1doykulvqjsyM2_gZn1SL7L4SreHZUq4Ixbg530hn5mQ"
BASE_PATH = os.path.dirname(__file__)
CREDENTIALS_FILE = os.path.join(BASE_PATH, 'credentials.json')

# The fresh data we just scraped via browser
MASTER_DATA = [
  {
    "name": "OnEMI Technology Solutions Ltd. (Kissht)",
    "type": "Mainboard",
    "open_date": "30-Apr-2026",
    "close_date": "05-May-2026",
    "listing_date": "08-May-2026",
    "price_band": "₹162 - ₹171",
    "lot_size": "87",
    "issue_size": "926 Cr",
    "gmp": "₹4.5",
    "revenue": "₹1,583.93 Cr",
    "profit": "₹199.27 Cr",
    "debt": "₹2,047.52 Cr",
    "about": "Technology-enabled lender offering digital loans through Kissht and Ring apps.",
    "status": "Active"
  },
  {
    "name": "Bagmane REIT",
    "type": "Mainboard",
    "open_date": "05-May-2026",
    "close_date": "07-May-2026",
    "listing_date": "15-May-2026",
    "price_band": "₹95 - ₹100",
    "issue_size": "3,405 Cr",
    "gmp": "₹4.5",
    "about": "Manages premium Grade A+ business parks in Bengaluru.",
    "status": "Upcoming"
  },
  {
    "name": "Recode Studios Ltd.",
    "type": "SME",
    "open_date": "05-May-2026",
    "close_date": "07-May-2026",
    "listing_date": "12-May-2026",
    "price_band": "₹150 - ₹158",
    "lot_size": "800",
    "issue_size": "45 Cr",
    "gmp": "₹35",
    "revenue": "₹57.45 Cr",
    "profit": "₹9.06 Cr",
    "debt": "₹7.56 Cr",
    "about": "Sells cosmetic products through an omnichannel network.",
    "status": "Upcoming"
  },
  {
    "name": "Value 360 Communications Ltd.",
    "type": "SME",
    "open_date": "04-May-2026",
    "close_date": "06-May-2026",
    "listing_date": "11-May-2026",
    "price_band": "₹95 - ₹98",
    "lot_size": "1,200",
    "issue_size": "42 Cr",
    "gmp": "₹0",
    "revenue": "₹55.08 Cr",
    "profit": "₹7.62 Cr",
    "debt": "₹16.67 Cr",
    "about": "Integrated marketing and PR solutions provider.",
    "status": "Upcoming"
  },
  {
    "name": "Leapfrog Engineering Services Ltd.",
    "type": "SME",
    "open_date": "TBD",
    "close_date": "TBD",
    "price_band": "₹21 - ₹23",
    "lot_size": "6,000",
    "issue_size": "89 Cr",
    "revenue": "₹105.05 Cr",
    "profit": "₹14.18 Cr",
    "debt": "₹32.22 Cr",
    "about": "Provides integrated engineering and EPCC services.",
    "status": "Upcoming"
  },
  {
    "name": "Amba Auto Sales & Services Ltd.",
    "type": "SME",
    "open_date": "27-Apr-2026",
    "close_date": "29-Apr-2026",
    "listing_date": "05-May-2026",
    "price_band": "₹130 - ₹135",
    "lot_size": "1,000",
    "issue_size": "65 Cr",
    "gmp": "₹0",
    "revenue": "₹203.79 Cr",
    "profit": "₹12.11 Cr",
    "debt": "₹35.84 Cr",
    "about": "Authorised dealer of Bajaj Auto and LG Electronics.",
    "status": "Closed"
  }
]

def update_everything():
    # 1. Save to JSON for App
    with open(os.path.join(BASE_PATH, 'ipos.json'), 'w') as f:
        json.dump(MASTER_DATA, f, indent=4)
    print("Local JSON updated.")

    # 2. Update Google Sheet
    scope = ["https://spreadsheets.google.com/feeds", "https://www.googleapis.com/auth/drive"]
    creds = ServiceAccountCredentials.from_json_keyfile_name(CREDENTIALS_FILE, scope)
    client = gspread.authorize(creds)
    sh = client.open_by_key(SPREADSHEET_ID)

    for tab_name in ["Mainboard", "SME"]:
        ws = sh.worksheet(tab_name)
        ws.clear()
        ws.append_row(["Name", "Price Band", "Lot Size", "GMP", "Open Date", "Listing", "Revenue", "Profit", "About"])
        data = [
            [i['name'], i['price_band'], i.get('lot_size', 'TBA'), i.get('gmp', 'TBA'), i['open_date'], i.get('listing_date', 'TBA'), i.get('revenue', 'TBA'), i.get('profit', 'TBA'), i['about']] 
            for i in MASTER_DATA if i['type'] == tab_name
        ]
        if data:
            ws.append_rows(data)
            print(f"Updated {tab_name} tab.")

if __name__ == "__main__":
    update_everything()
