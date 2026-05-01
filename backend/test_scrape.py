import requests
import re
import json
from bs4 import BeautifulSoup

def test_mb():
    url = 'https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/mainboard/'
    headers = {'User-Agent': 'Mozilla/5.0'}
    r = requests.get(url, headers=headers)
    
    # Try to find table tags
    soup = BeautifulSoup(r.content, 'html.parser')
    tables = soup.find_all('table')
    print(f"Found {len(tables)} tables in source HTML")
    
    # Try to find Next.js flight data
    matches = re.findall(r'self\.__next_f\.push\(\[1,\"(.*?)\"\]\)', r.text)
    print(f"Found {len(matches)} flight data segments")
    
    if len(matches) > 0:
        # Flight data is usually escaped strings that look like JSON fragments
        # This is very messy to parse, but let's see if we can find IPO names
        all_text = "".join(matches)
        if "OnEMI" in all_text:
            print("Found 'OnEMI' in flight data!")
        else:
            print("Could not find 'OnEMI' in flight data.")

if __name__ == "__main__":
    test_mb()
