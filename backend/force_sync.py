import json
import os
import gspread
from oauth2client.service_account import ServiceAccountCredentials

# Data fetched from Browser Subagent
MASTER_IPOS = [
    {
        "id": "IPO_OnEMI_Kissht",
        "name": "OnEMI Technology (Kissht)",
        "price": "₹162 - ₹171",
        "lotSize": "87",
        "gmp": "₹4.5",
        "type": "Mainboard",
        "status": "Open",
        "openDate": "30-Apr-2026",
        "closeDate": "05-May-2026",
        "listingDate": "08-May-2026",
        "logoUrl": "https://logo.clearbit.com/kissht.com?size=128"
    },
    {
        "id": "IPO_Bagmane_REIT",
        "name": "Bagmane REIT",
        "price": "₹100",
        "lotSize": "TBA",
        "gmp": "₹4.5",
        "type": "Mainboard",
        "status": "Upcoming",
        "openDate": "05-May-2026",
        "closeDate": "07-May-2026",
        "listingDate": "15-May-2026",
        "logoUrl": "https://logo.clearbit.com/bagmane.com?size=128"
    },
    {
        "id": "IPO_Recode_Studios",
        "name": "Recode Studios Ltd",
        "price": "₹150 - ₹158",
        "lotSize": "800",
        "gmp": "₹35",
        "type": "SME",
        "status": "Upcoming",
        "openDate": "05-May-2026",
        "closeDate": "07-May-2026",
        "listingDate": "12-May-2026",
        "logoUrl": "https://logo.clearbit.com/recode.com?size=128"
    },
    {
        "id": "IPO_Value_360",
        "name": "Value 360 Communications",
        "price": "₹95 - ₹98",
        "lotSize": "1200",
        "gmp": "₹0",
        "type": "SME",
        "status": "Upcoming",
        "openDate": "04-May-2026",
        "closeDate": "06-May-2026",
        "listingDate": "11-May-2026",
        "logoUrl": "https://logo.clearbit.com/value.com?size=128"
    }
]

MASTER_BUYBACKS = [
    {"name": "Windlas Biotech", "price": "₹640", "open": "30-Apr", "close": "07-May", "status": "Live"},
    {"name": "Jagsonpal Pharma", "price": "₹250", "open": "Record: 04-May", "close": "-", "status": "Upcoming"},
    {"name": "Wipro Ltd", "price": "₹250", "open": "TBA", "close": "TBA", "status": "Upcoming"},
    {"name": "Cyient Ltd", "price": "₹1125", "open": "TBA", "close": "TBA", "status": "Upcoming"},
    {"name": "Rolex Rings", "price": "₹180", "open": "TBA", "close": "TBA", "status": "Upcoming"},
    {"name": "Kajaria Ceramics", "price": "₹1380", "open": "TBA", "close": "TBA", "status": "Upcoming"}
]

MASTER_NEWS = [
    {"id": "N1", "headline": "OnEMI (Kissht) IPO: Should you subscribe?", "summary": "Moneycontrol analysis of the upcoming fintech IPO.", "imageUrl": "https://logo.clearbit.com/moneycontrol.com", "date": "01-May-2026"},
    {"id": "N2", "headline": "Kissht operator mobilises ₹278 crore from anchor investors.", "summary": "Strong demand seen in the anchor book ahead of public issue.", "imageUrl": "https://logo.clearbit.com/business-standard.com", "date": "01-May-2026"},
    {"id": "N3", "headline": "Recode Studios IPO to hit Dalal Street next week.", "summary": "SME beauty brand looking to raise funds for expansion.", "imageUrl": "https://logo.clearbit.com/recode.com", "date": "01-May-2026"},
    {"id": "N4", "headline": "Wipro and Cyient announce mega buyback offers.", "summary": "IT giants return value to shareholders via tender route.", "imageUrl": "https://logo.clearbit.com/wipro.com", "date": "01-May-2026"}
]

SPREADSHEET_ID = "1doykulvqjsyM2_gZn1SL7L4SreHZUq4Ixbg530hn5mQ"
CREDENTIALS_FILE = os.path.join(os.path.dirname(__file__), 'credentials.json')

def sync_all():
    # Save to JSON
    with open('ipos.json', 'w') as f: json.dump(MASTER_IPOS, f, indent=4)
    with open('buybacks.json', 'w') as f: json.dump(MASTER_BUYBACKS, f, indent=4)
    with open('news.json', 'w') as f: json.dump(MASTER_NEWS, f, indent=4)
    print("Local JSONs updated.")

    # Sync to GSheet
    if os.path.exists(CREDENTIALS_FILE):
        scope = ["https://spreadsheets.google.com/feeds", "https://www.googleapis.com/auth/drive"]
        creds = ServiceAccountCredentials.from_json_keyfile_name(CREDENTIALS_FILE, scope)
        client = gspread.authorize(creds)
        sh = client.open_by_key(SPREADSHEET_ID)

        # Mainboard
        ws = sh.worksheet("Mainboard")
        ws.clear()
        ws.append_row(["Name", "Price", "Lot Size", "GMP", "Status"])
        ws.append_rows([[i['name'], i['price'], i['lotSize'], i['gmp'], i['status']] for i in MASTER_IPOS if i['type'] == 'Mainboard'])

        # SME
        ws = sh.worksheet("SME")
        ws.clear()
        ws.append_row(["Name", "Price", "Lot Size", "GMP", "Status"])
        ws.append_rows([[i['name'], i['price'], i['lotSize'], i['gmp'], i['status']] for i in MASTER_IPOS if i['type'] == 'SME'])

        # Buybacks
        ws = sh.worksheet("Buybacks")
        ws.clear()
        ws.append_row(["Company", "Price", "Open", "Close", "Status"])
        ws.append_rows([[b['name'], b['price'], b['open'], b['close'], b['status']] for b in MASTER_BUYBACKS])

        # News
        ws = sh.worksheet("News")
        ws.clear()
        ws.append_row(["Headline", "Summary", "Date"])
        ws.append_rows([[n['headline'], n['summary'], n['date']] for n in MASTER_NEWS])

        print("Google Sheets updated successfully!")

if __name__ == "__main__":
    sync_all()
