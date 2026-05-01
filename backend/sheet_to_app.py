import json
import os
import gspread
from oauth2client.service_account import ServiceAccountCredentials

# Configuration
SPREADSHEET_ID = "1doykulvqjsyM2_gZn1SL7L4SreHZUq4Ixbg530hn5mQ"
BASE_PATH = os.path.dirname(__file__)
CREDENTIALS_FILE = os.path.join(BASE_PATH, 'credentials.json')

def get_gspread_client():
    if not os.path.exists(CREDENTIALS_FILE):
        print(f"Error: {CREDENTIALS_FILE} not found.")
        return None
    scope = ["https://spreadsheets.google.com/feeds", "https://www.googleapis.com/auth/drive"]
    creds = ServiceAccountCredentials.from_json_keyfile_name(CREDENTIALS_FILE, scope)
    return gspread.authorize(creds)

def sync_sheet_to_app():
    client = get_gspread_client()
    if not client:
        return

    try:
        print(f"Opening Spreadsheet: {SPREADSHEET_ID}")
        sh = client.open_by_key(SPREADSHEET_ID)
        
        all_ipos = []
        
        # 1. Fetch Mainboard IPOs
        print("Fetching Mainboard IPOs...")
        try:
            ws_main = sh.worksheet("Mainboard")
            rows = ws_main.get_all_records()
            for row in rows:
                all_ipos.append({
                    "id": f"IPO_{row.get('Name', 'Unknown').replace(' ', '_')[:20]}",
                    "name": row.get('Name', 'Unknown'),
                    "offerPrice": row.get('Price', row.get('Offer Price', 'TBA')),
                    "lotSize": str(row.get('Lot Size', 'TBA')),
                    "gmp": row.get('GMP', 'TBA'),
                    "type": "Mainboard",
                    "status": row.get('Status', 'Upcoming'),
                    "openDate": row.get('Open Date', None),
                    "closeDate": row.get('Close Date', None),
                    "listingDate": row.get('Listing Date', None)
                })
        except Exception as e:
            print(f"Error reading Mainboard tab: {e}")

        # 2. Fetch SME IPOs
        print("Fetching SME IPOs...")
        try:
            ws_sme = sh.worksheet("SME")
            rows = ws_sme.get_all_records()
            for row in rows:
                all_ipos.append({
                    "id": f"IPO_{row.get('Name', 'Unknown').replace(' ', '_')[:20]}",
                    "name": row.get('Name', 'Unknown'),
                    "offerPrice": row.get('Price', row.get('Offer Price', 'TBA')),
                    "lotSize": str(row.get('Lot Size', 'TBA')),
                    "gmp": row.get('GMP', 'TBA'),
                    "type": "SME",
                    "status": row.get('Status', 'Upcoming'),
                    "openDate": row.get('Open Date', None),
                    "closeDate": row.get('Close Date', None),
                    "listingDate": row.get('Listing Date', None)
                })
        except Exception as e:
            print(f"Error reading SME tab: {e}")

        # 3. Fetch Buybacks
        print("Fetching Buybacks...")
        buybacks = []
        try:
            ws_bb = sh.worksheet("Buybacks")
            rows = ws_bb.get_all_records()
            for row in rows:
                buybacks.append({
                    "id": f"BB_{row.get('Company', 'Unknown').replace(' ', '_')[:20]}",
                    "name": row.get('Company', row.get('Name', 'Unknown')),
                    "buybackPrice": row.get('Price', 'TBA'),
                    "openDate": row.get('Open', 'TBA'),
                    "closeDate": row.get('Close', 'TBA'),
                    "status": row.get('Status', 'Upcoming')
                })
        except Exception as e:
            print(f"Error reading Buybacks tab: {e}")

        # 4. Fetch News
        print("Fetching News...")
        news = []
        try:
            ws_news = sh.worksheet("News")
            rows = ws_news.get_all_records()
            for i, row in enumerate(rows):
                news.append({
                    "id": str(i + 1),
                    "headline": row.get('Headline', 'Market Update'),
                    "summary": row.get('Summary', 'No summary available.'),
                    "imageUrl": "https://images.unsplash.com/photo-1611974714658-66d2c132042e?auto=format&fit=crop&q=80&w=300",
                    "date": row.get('Date', 'Recently')
                })
        except Exception as e:
            print(f"Error reading News tab: {e}")

        # Save to JSON (This overwrites/deletes old data as requested)
        with open(os.path.join(BASE_PATH, 'ipos.json'), 'w') as f:
            json.dump(all_ipos, f, indent=4)
        with open(os.path.join(BASE_PATH, 'buybacks.json'), 'w') as f:
            json.dump(buybacks, f, indent=4)
        with open(os.path.join(BASE_PATH, 'news.json'), 'w') as f:
            json.dump(news, f, indent=4)

        print("\nSUCCESS: All data synced from Google Sheet to App JSONs!")
        print(f"IPOs: {len(all_ipos)}, Buybacks: {len(buybacks)}, News: {len(news)}")

    except Exception as e:
        print(f"General Sync Error: {e}")

if __name__ == "__main__":
    sync_sheet_to_app()
