import requests
from bs4 import BeautifulSoup

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7',
    'Accept-Language': 'en-US,en;q=0.9',
    'Cache-Control': 'max-age=0',
    'Connection': 'keep-alive',
    'Upgrade-Insecure-Requests': '1',
}

url = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/mainboard/"

try:
    response = requests.get(url, headers=headers, timeout=20)
    print(f"Status Code: {response.status_code}")
    print(f"Response Content Length: {len(response.content)}")
    
    with open("debug_chittorgarh.html", "wb") as f:
        f.write(response.content)
        
    soup = BeautifulSoup(response.content, 'html.parser')
    table = soup.find('table')
    if table:
        print("Table found!")
        rows = table.find_all('tr')
        print(f"Number of rows: {len(rows)}")
    else:
        print("No table found.")
        # Check for common "Blocked" text
        if "Cloudflare" in response.text:
            print("Blocked by Cloudflare")
        elif "forbidden" in response.text.lower():
            print("Access Forbidden")
        else:
            print("Unknown issue. Check debug_chittorgarh.html")

except Exception as e:
    print(f"Error: {e}")
