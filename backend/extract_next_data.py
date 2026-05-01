from bs4 import BeautifulSoup
import json

with open("debug_chittorgarh.html", "rb") as f:
    soup = BeautifulSoup(f.read(), 'html.parser')

scripts = soup.find_all('script')
for s in scripts:
    if s.get('id'):
        print(f"ID: {s.get('id')}")
    if '__NEXT_DATA__' in s.text:
        print("Found __NEXT_DATA__ in text!")
        try:
            data = json.loads(s.text)
            print("Successfully parsed JSON!")
            # Print keys of the first few levels
            print(f"Keys: {data.keys()}")
            if 'props' in data:
                print(f"Props keys: {data['props'].keys()}")
                if 'pageProps' in data['props']:
                    print(f"pageProps keys: {data['props']['pageProps'].keys()}")
                    if 'resultData' in data['props']['pageProps']:
                        print("Found resultData!")
                        rd = data['props']['pageProps']['resultData']
                        if 'reportData' in rd:
                            print(f"reportData length: {len(rd['reportData'])}")
                            if len(rd['reportData']) > 0:
                                print(f"First record: {rd['reportData'][0]}")
        except Exception as e:
            print(f"JSON Parse Error: {e}")
