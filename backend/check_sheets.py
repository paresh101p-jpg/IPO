import gspread
import os
from oauth2client.service_account import ServiceAccountCredentials

def check_all_sheets():
    BASE_PATH = os.path.dirname(__file__)
    CREDENTIALS_FILE = os.path.join(BASE_PATH, 'credentials.json')
    
    if not os.path.exists(CREDENTIALS_FILE):
        print("Credentials file not found!")
        return

    scope = ["https://spreadsheets.google.com/feeds", "https://www.googleapis.com/auth/drive"]
    creds = ServiceAccountCredentials.from_json_keyfile_name(CREDENTIALS_FILE, scope)
    client = gspread.authorize(creds)

    print("Listing all spreadsheets available to this service account with IDs:")
    files = client.list_spreadsheet_files()
    for f in files:
        print(f"Name: {f['name']} | ID: {f['id']}")

if __name__ == "__main__":
    check_all_sheets()
