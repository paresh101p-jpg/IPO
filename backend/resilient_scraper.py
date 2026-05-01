import json
import re
import requests
from bs4 import BeautifulSoup
import os
import time

# Use a Mobile User-Agent which is often less restricted
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/604.1',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
}

def scrape_ipowatch_all():
    print("Scraping IPOWatch (Resilient Mode)...")
    url = "https://ipowatch.in/ipo-grey-market-premium-latest-ipo-gmp/"
    ipos = []
    try:
        r = requests.get(url, headers=HEADERS, timeout=20)
        soup = BeautifulSoup(r.content, 'html.parser')
        
        # IPOWatch uses <table> with specific structure
        tables = soup.find_all('table')
        if not tables:
            print("No tables found on IPOWatch.")
            return []
            
        # The main table is usually the first one
        table = tables[0]
        rows = table.find_all('tr')[1:]
        for row in rows:
            cols = row.find_all(['td', 'th'])
            if len(cols) >= 4:
                name = cols[0].text.strip()
                gmp = cols[1].text.strip()
                price = cols[2].text.strip()
                lot = cols[4].text.strip() if len(cols) > 4 else "TBA"
                
                # Cleanup name
                clean_name = re.sub(r'\(.*?\)', '', name).strip()
                ipo_type = "SME" if "SME" in name or "(SME)" in name else "Mainboard"
                
                ipos.append({
                    "id": f"IPO_{clean_name.replace(' ', '_')[:20]}",
                    "name": clean_name,
                    "type": ipo_type,
                    "status": "Live",
                    "logoUrl": f"https://logo.clearbit.com/{re.sub(r'[^a-z]', '', clean_name.split()[0].lower())}.com?size=128",
                    "offerPrice": f"Rs {price}",
                    "lotSize": lot,
                    "gmp": f"Rs {gmp}" if "Rs" not in gmp else gmp,
                    "listingDate": "TBA",
                    "openDate": "TBA",
                    "closeDate": "TBA",
                })
        return ipos
    except Exception as e:
        print(f"IPOWatch Error: {e}")
        return []

def main():
    ipos = scrape_ipowatch_all()
    
    # Save to file
    base_path = os.path.dirname(__file__)
    with open(os.path.join(base_path, 'ipos.json'), 'w') as f:
        json.dump(ipos, f, indent=4)
        
    print(f"Successfully scraped {len(ipos)} IPOs.")

if __name__ == "__main__":
    main()
