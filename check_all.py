import os
import glob
from PIL import Image

dir_path = r"C:\Users\Admin\.gemini\antigravity\brain\ed67a871-f973-453c-9095-25590f5c9e50"
for f in glob.glob(os.path.join(dir_path, "media__*.png")):
    try:
        img = Image.open(f)
        print(f"{os.path.basename(f)}: {img.size}")
    except Exception as e:
        print(f"Failed to open {f}: {e}")
