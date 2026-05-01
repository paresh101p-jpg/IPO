import requests
from bs4 import BeautifulSoup

url = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/mainboard/"
headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'}

resp = requests.get(url, headers=headers)
print(f"Status Code: {resp.status_code}")
soup = BeautifulSoup(resp.content, 'html.parser')
tables = soup.find_all('table')
print(f"Found {len(tables)} tables")
for i, table in enumerate(tables):
    print(f"Table {i} class: {table.get('class')}")
    # Print first few chars of table text
    print(f"Table {i} text: {table.text.strip()[:100]}...")

with open('debug_chittorgarh.html', 'wb') as f:
    f.write(resp.content)
