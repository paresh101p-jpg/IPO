import requests
from bs4 import BeautifulSoup

def test_fetch():
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7',
        'Accept-Language': 'en-US,en;q=0.9',
        'Cache-Control': 'max-age=0',
        'Connection': 'keep-alive',
        'Upgrade-Insecure-Requests': '1',
        'Referer': 'https://www.google.com/',
    }
    
    session = requests.Session()
    session.headers.update(headers)
    
    urls = [
        "https://www.investorgain.com/report/live-ipo-gmp/331/ipo/",
        "https://www.investorgain.com/report/live-ipo-gmp/331/sme/",
    ]
    
    for url in urls:
        print(f"Fetching {url}...")
        try:
            resp = session.get(url, timeout=20)
            print(f"Status: {resp.status_code}")
            if "Access Forbidden" in resp.text:
                print("STILL FORBIDDEN")
            else:
                with open(f"debug_{url.split('/')[-2]}.html", "wb") as f:
                    f.write(resp.content)
                soup = BeautifulSoup(resp.content, 'html.parser')
                table = soup.find('table')
                if table:
                    print("Table found!")
                else:
                    print("No table found.")
        except Exception as e:
            print(f"Error: {e}")

if __name__ == "__main__":
    test_fetch()
