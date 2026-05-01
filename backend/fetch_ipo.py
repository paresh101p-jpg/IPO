import requests
from bs4 import BeautifulSoup

def fetch_mainboard_ipos():
    url = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/mainboard/?year=2026"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
    resp = requests.get(url, headers=headers, timeout=15)
    print("Status:", resp.status_code)
    if resp.status_code != 200:
        print("Failed to fetch page")
        return

    soup = BeautifulSoup(resp.content, "html.parser")
    table = soup.find("table")
    if not table:
        print("No table found")
        return

    rows = table.find_all("tr")[1:]  # skip header
    for i, row in enumerate(rows, 1):
        cols = row.find_all(["td", "th"])
        if len(cols) < 5:
            continue
        name = cols[0].text.strip()
        open_dt = cols[1].text.strip()
        close_dt = cols[2].text.strip()
        price = cols[4].text.strip()
        lot = cols[5].text.strip() if len(cols) > 5 else "N/A"
        print(f"{i}. {name} - Open:{open_dt} Close:{close_dt} Price:{price} Lot:{lot}")

if __name__ == "__main__":
    fetch_mainboard_ipos()