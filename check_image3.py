import os
from PIL import Image

dir_path = r"C:\Users\Admin\.gemini\antigravity\brain\ed67a871-f973-453c-9095-25590f5c9e50"
img_path = os.path.join(dir_path, "media__1777449866109.png")

img = Image.open(img_path)
print(f"Image 3: {img_path} - Size: {img.size}")
