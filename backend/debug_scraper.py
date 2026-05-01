import json
import re
import requests
from bs4 import BeautifulSoup
import os

SESSION = requests.Session()
HEADERS = {'User-Agent': 'Mozilla/5.0'}

def debug_scrape():
    url = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/mainboard/"
    r = SESSION.get(url, headers=HEADERS)
    soup = BeautifulSoup(r.content, 'html.parser')
    table = soup.find('table')
    if not table:
        print("No table found")
        return
    
    rows = table.find_all('tr')
    print(f"Found {len(rows)} rows")
    for i, row in enumerate(rows[:5]):
        cols = row.find_all(['td', 'th'])
        print(f"Row {i} has {len(cols)} columns: {[c.text.strip() for c in cols]}")

if __name__ == "__main__":
    debug_scrape()
