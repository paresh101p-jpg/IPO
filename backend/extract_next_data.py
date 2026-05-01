from bs4 import BeautifulSoup
import json

with open("debug_chittorgarh.html", "rb") as f:
    soup = BeautifulSoup(f.read(), 'html.parser')

scripts = soup.find_all('script')
print(f"Found {len(scripts)} scripts")
for s in scripts:
    script_id = s.get('id', 'No ID')
    if '__NEXT_DATA__' in s.text or script_id == '__NEXT_DATA__':
        print(f"Found __NEXT_DATA__ in script with ID: {script_id}")
        try:
            data = json.loads(s.text)
            print("Successfully parsed JSON!")
            # Navigate to the data
            if 'props' in data and 'pageProps' in data['props']:
                pp = data['props']['pageProps']
                if 'resultData' in pp and 'reportData' in pp['resultData']:
                    rd = pp['resultData']['reportData']
                    print(f"Records found: {len(rd)}")
                    if len(rd) > 0:
                        print(f"First Record: {rd[0]}")
        except Exception as e:
            print(f"Error parsing: {e}")
