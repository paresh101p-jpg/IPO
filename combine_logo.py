import os
from PIL import Image

dir_path = r"C:\Users\Admin\.gemini\antigravity\brain\ed67a871-f973-453c-9095-25590f5c9e50"
arrow_path = os.path.join(dir_path, "media__1777449923719.png")
text_path = os.path.join(dir_path, "media__1777449892171.png")

arrow_img = Image.open(arrow_path).convert("RGBA")
text_img = Image.open(text_path).convert("RGBA")

# Create a new image large enough to hold both, with transparent background
# Arrow is 317x514, text is 231x48
new_width = max(arrow_img.width, text_img.width) + 40
new_height = arrow_img.height + text_img.height + 60

combined = Image.new("RGBA", (new_width, new_height), (0, 0, 0, 0))

# Paste arrow at the top center
arrow_x = (new_width - arrow_img.width) // 2
arrow_y = 10
combined.paste(arrow_img, (arrow_x, arrow_y), arrow_img)

# Paste text below arrow, center
text_x = (new_width - text_img.width) // 2
text_y = arrow_y + arrow_img.height + 10
combined.paste(text_img, (text_x, text_y), text_img)

out_path = r"e:\IPO\app\src\main\res\drawable\ipo_logo.png"
combined.save(out_path, "PNG")
print(f"Saved combined logo to {out_path}")
