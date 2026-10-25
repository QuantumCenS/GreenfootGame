"""Gera todas as imagens pixel-art do projeto (fundos, texturas, sprites)."""
import math, random, os, sys
from PIL import Image, ImageDraw
import numpy as np

OUT = sys.argv[1] if len(sys.argv) > 1 else "images"
os.makedirs(OUT, exist_ok=True)

BAYER = np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]) / 16.0 + 1 / 32


def up(img, s):
    return img.resize((img.width * s, img.height * s), Image.NEAREST)


def lerp(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(len(a)))


def dith(t, x, y, steps):
    v = max(0.0, min(1.0, t)) * (steps - 1)
    b = int(v)
    if v - b > BAYER[y % 4][x % 4]:
        b += 1
    return min(b, steps - 1) / (steps - 1)


def gradient(img, box, stops, steps=10, vertical=True):
    """stops: lista [(t, cor)]. Preenche a caixa com degradê com dithering."""
    x0, y0, x1, y1 = box
    px = img.load()
    for y in range(y0, y1):
        for x in range(x0, x1):
            t = (y - y0) / max(1, (y1 - y0 - 1)) if vertical else (x - x0) / max(1, (x1 - x0 - 1))
            t = dith(t, x, y, steps)
            for i in range(len(stops) - 1):
                if stops[i][0] <= t <= stops[i + 1][0]:
                    lt = (t - stops[i][0]) / max(1e-6, stops[i + 1][0] - stops[i][0])
                    px[x, y] = lerp(stops[i][1], stops[i + 1][1], lt) + ((255,) if img.mode == "RGBA" else ())
                    break


def blend_px(px, x, y, col, a, W, H):
    if 0 <= x < W and 0 <= y < H:
        c = px[x, y]
        px[x, y] = tuple(int(c[i] * (1 - a) + col[i] * a) for i in range(3)) + c[3:]


def glow(img, cx, cy, r, col, strength=0.6, steps=6):
    px = img.load()
    W, H = img.size
    for y in range(int(cy - r), int(cy + r) + 1):
        for x in range(int(cx - r), int(cx + r) + 1):
            d = math.hypot(x - cx, y - cy) / r
            if d < 1:
                a = dith((1 - d) ** 2 * strength, x, y, steps)
                blend_px(px, x, y, col, a, W, H)


def outline(img, col=(14, 12, 22, 255)):
    a = np.array(img)
    alpha = a[:, :, 3] > 0
    o = np.zeros_like(alpha)
    o[1:, :] |= alpha[:-1, :]
    o[:-1, :] |= alpha[1:, :]
    o[:, 1:] |= alpha[:, :-1]
    o[:, :-1] |= alpha[:, 1:]
    o &= ~alpha
    a[o] = col
    return Image.fromarray(a)


# ----------------------------------------------------------------------------------
# FUNDO NÍVEL 1 — Docas de Zanarkin (noite, lua doentia, tentáculos no mar)
# ----------------------------------------------------------------------------------
W, H = 256, 143
HORIZ = 92


def house_row(d, rnd, x0, x1, base, hmin, hmax, wall, roof, win_on, lit=0.3, wmin=10, wmax=18):
    x = x0
    while x < x1:
        w = rnd.randint(wmin, wmax)
        h = rnd.randint(hmin, hmax)
        top = base - h
        d.rectangle([x, top, x + w - 1, base], fill=wall)
        # telhado de quatro águas (estilo madeirense)
        d.polygon([(x - 1, top), (x + w, top), (x + w - 3, top - 4), (x + 2, top - 4)], fill=roof)
        for wy in range(top + 3, base - 2, 5):
            for wx in range(x + 2, x + w - 2, 4):
                if rnd.random() < lit:
                    d.rectangle([wx, wy, wx + 1, wy + 1], fill=win_on)
                elif rnd.random() < 0.5:
                    d.rectangle([wx, wy, wx + 1, wy + 1], fill=lerp(wall, (0, 0, 0), 0.35))
        x += w + rnd.randint(-2, 2)


def fundo_docas(escuro=0.0):
    rnd = random.Random(7)
    img = Image.new("RGB", (W, H))
    gradient(img, (0, 0, W, HORIZ), [(0, (10, 10, 30)), (0.55, (26, 30, 62)), (1, (52, 74, 88))], steps=12)
    px = img.load()
    # estrelas
    for _ in range(140):
        x, y = rnd.randrange(W), rnd.randrange(HORIZ - 20)
        c = rnd.choice([(200, 210, 230), (150, 160, 200), (230, 220, 180), (120, 140, 190)])
        px[x, y] = c
        if rnd.random() < 0.08 and 1 <= x < W - 1 and 1 <= y:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                px[x + dx, y + dy] = lerp(c, px[x + dx, y + dy], 0.5)
    # lua doentia esverdeada com halo
    mx, my, mr = 196, 30, 17
    glow(img, mx, my, 60, (90, 140, 120), 0.35, 8)
    glow(img, mx, my, 32, (150, 200, 170), 0.5, 8)
    d = ImageDraw.Draw(img)
    d.ellipse([mx - mr, my - mr, mx + mr, my + mr], fill=(214, 232, 200))
    for (cx, cy, r) in ((-6, -4, 4), (5, 3, 5), (-2, 8, 3), (8, -8, 2), (-10, 5, 2)):
        d.ellipse([mx + cx - r, my + cy - r, mx + cx + r, my + cy + r], fill=(184, 206, 176))
    for y in range(my - mr, my + mr + 1):  # sombra lateral
        for x in range(mx - mr, mx + mr + 1):
            dd = math.hypot(x - mx, y - my)
            if dd <= mr and (x - mx) + (y - my) * 0.3 > mr * 0.35 and BAYER[y % 4][x % 4] < 0.6:
                px[x, y] = lerp(px[x, y], (130, 160, 140), 0.6)
    # nuvens finas roxas
    for (cx, cy, w) in ((170, 36, 70), (60, 22, 90), (120, 52, 60), (230, 58, 50)):
        for i in range(w):
            x = cx - w // 2 + i
            yy = cy + int(2 * math.sin(i * 0.15))
            for t in range(3):
                if 0 <= x < W and BAYER[(yy + t) % 4][x % 4] < (0.55 - t * 0.15):
                    px[x, yy + t] = lerp(px[x, yy + t], (70, 60, 100), 0.7)
    # promontório e farol à esquerda
    d.polygon([(0, 60), (14, 58), (30, 64), (46, 72), (62, 82), (78, HORIZ), (0, HORIZ)], fill=(22, 26, 40))
    d.polygon([(0, 70), (20, 70), (40, 78), (56, HORIZ), (0, HORIZ)], fill=(18, 20, 32))
    d.rectangle([16, 40, 21, 58], fill=(46, 48, 64))
    d.rectangle([16, 44, 21, 45], fill=(120, 40, 46))
    d.rectangle([16, 50, 21, 51], fill=(120, 40, 46))
    d.rectangle([15, 36, 22, 40], fill=(30, 30, 40))
    d.rectangle([17, 37, 20, 39], fill=(255, 236, 160))
    for i in range(70):  # feixe do farol
        y0, y1 = 38 - i * 0.12, 38 + i * 0.25
        for y in range(int(y0), int(y1) + 1):
            x = 22 + i
            if 0 <= x < W and 0 <= y < H and BAYER[y % 4][x % 4] < 0.35 * (1 - i / 70):
                px[x, y] = lerp(px[x, y], (255, 236, 170), 0.35)
    # vila ao fundo (casas em encosta, à direita)
    house_row(d, rnd, 96, 260, HORIZ - 14, 10, 18, (34, 38, 58), (58, 34, 40), (230, 190, 100), lit=0.25)
    house_row(d, rnd, 120, 260, HORIZ - 4, 8, 14, (42, 46, 68), (74, 40, 44), (250, 210, 120), lit=0.3)
    # torre de igreja
    d.rectangle([150, 50, 158, HORIZ - 14], fill=(36, 40, 60))
    d.polygon([(149, 50), (159, 50), (154, 40)], fill=(58, 34, 40))
    d.rectangle([153, 55, 155, 58], fill=(240, 200, 110))
    house_row(d, rnd, 80, 140, HORIZ, 6, 10, (48, 52, 74), (82, 46, 48), (250, 214, 130), lit=0.2)
    # nevoeiro na linha do horizonte
    for y in range(HORIZ - 10, HORIZ + 2):
        for x in range(W):
            a = (1 - abs(y - (HORIZ - 3)) / 8)
            if a > 0 and BAYER[y % 4][x % 4] < a * 0.5:
                px[x, y] = lerp(px[x, y], (80, 100, 110), 0.45)
    # mar
    gradient(img, (0, HORIZ, W, H), [(0, (30, 52, 70)), (0.4, (18, 34, 52)), (1, (8, 16, 30))], steps=10)
    px = img.load()
    for _ in range(260):  # ondulação
        x, y = rnd.randrange(W), rnd.randrange(HORIZ + 2, H)
        l = rnd.randint(2, 6)
        for i in range(l):
            if x + i < W:
                px[x + i, y] = lerp(px[x + i, y], (60, 96, 110), 0.6)
    for y in range(HORIZ + 1, H, 2):  # reflexo da lua
        spread = 6 + (y - HORIZ) * 0.35
        for _ in range(3):
            x = int(mx + rnd.uniform(-spread, spread))
            l = rnd.randint(2, 7)
            for i in range(l):
                if 0 <= x + i < W:
                    px[x + i, y] = lerp(px[x + i, y], (190, 220, 180), 0.7 - (y - HORIZ) / 120)
    # tentáculos ao longe (horror cósmico)
    d = ImageDraw.Draw(img)
    for (bx, h, curl, s) in ((58, 26, 1, 3), (70, 34, -1, 4), (84, 20, 1, 3)):
        pts = []
        for i in range(h):
            t = i / h
            x = bx + curl * (math.sin(t * 3.2) * 6 + t * t * 8)
            pts.append((x, HORIZ + 4 - i, max(1, s * (1 - t) + 0.6)))
        for (x, y, w) in pts:
            d.ellipse([x - w, y - 1, x + w, y + 1], fill=(14, 22, 30))
        for (x, y, w) in pts[2::4]:
            if w > 1.5:
                px[int(x - curl), int(y)] = (40, 58, 60)
    # barcos de pesca (silhuetas)
    for (bx, by) in ((112, 100), (34, 104)):
        d.polygon([(bx, by), (bx + 12, by), (bx + 10, by + 3), (bx + 2, by + 3)], fill=(12, 16, 26))
        d.line([(bx + 6, by), (bx + 6, by - 9)], fill=(12, 16, 26))
        d.polygon([(bx + 7, by - 8), (bx + 7, by - 1), (bx + 11, by - 1)], fill=(30, 36, 50))
        px[bx + 3, by - 1] = (255, 200, 110)
    # vinheta
    a = np.array(img).astype(float)
    yy, xx = np.mgrid[0:H, 0:W]
    v = np.clip(((xx - W / 2) / (W * 0.62)) ** 2 + ((yy - H * 0.45) / (H * 0.85)) ** 2, 0, 1)
    a *= (1 - 0.45 * v ** 1.5)[..., None]
    a *= (1 - escuro)
    return up(Image.fromarray(a.clip(0, 255).astype(np.uint8)), 4)


# ----------------------------------------------------------------------------------
# FUNDO NÍVEL 2 — Templo Subterrâneo (catacumbas, fenda cósmica, abismo)
# ----------------------------------------------------------------------------------
def fundo_templo():
    rnd = random.Random(11)
    img = Image.new("RGB", (W, H))
    gradient(img, (0, 0, W, H), [(0, (12, 10, 22)), (0.6, (22, 18, 36)), (1, (14, 10, 26))], steps=8)
    d = ImageDraw.Draw(img)
    px = img.load()
    # tijolos da parede de fundo
    for row, y in enumerate(range(0, 118, 7)):
        off = (row % 2) * 7
        for x in range(-off, W, 14):
            shade = rnd.randint(-4, 5)
            c = (30 + shade, 26 + shade, 44 + shade)
            d.rectangle([x + 1, y + 1, x + 13, y + 6], fill=c)
            d.line([(x + 1, y + 1), (x + 12, y + 1)], fill=lerp(c, (60, 54, 80), 0.4))
    # arcos góticos escuros (nichos)
    for ax in (28, 92, 164, 228):
        d.rectangle([ax - 12, 46, ax + 12, 118], fill=(10, 8, 18))
        d.ellipse([ax - 12, 34, ax + 12, 58], fill=(10, 8, 18))
        for y in range(40, 118, 2):
            pass
        # estátua encapuçada no nicho
        d.ellipse([ax - 4, 62, ax + 4, 72], fill=(26, 22, 36))
        d.polygon([(ax - 7, 118), (ax + 7, 118), (ax + 5, 70), (ax - 5, 70)], fill=(26, 22, 36))
        px[ax - 1, 67] = (90, 230, 170)
        px[ax + 1, 67] = (90, 230, 170)
    # colunas
    for cx in (60, 128, 196):
        d.rectangle([cx - 7, 20, cx + 7, 120], fill=(40, 34, 56))
        d.rectangle([cx - 7, 20, cx - 5, 120], fill=(56, 50, 74))
        d.rectangle([cx + 5, 20, cx + 7, 120], fill=(28, 24, 40))
        d.rectangle([cx - 9, 16, cx + 9, 21], fill=(50, 44, 66))
        d.rectangle([cx - 9, 114, cx + 9, 120], fill=(50, 44, 66))
        # runas brilhantes
        for i, ry in enumerate(range(34, 104, 14)):
            g = [(cx - 1, ry), (cx + 1, ry), (cx, ry + 1), (cx, ry + 2), (cx - 1, ry + 3), (cx + 1, ry + 3)]
            if i % 2:
                g = [(cx, ry), (cx - 1, ry + 1), (cx + 1, ry + 1), (cx, ry + 2), (cx, ry + 3)]
            glow(img, cx, ry + 1.5, 6, (60, 200, 160), 0.35, 6)
            for p in g:
                px[p] = (130, 255, 200)
    # fenda cósmica no topo
    fx, fy = 128, 12
    glow(img, fx, fy, 70, (90, 40, 140), 0.55, 8)
    for i in range(6, 0, -1):
        rx, ry = 34 * i / 6, 13 * i / 6
        c = lerp((20, 6, 40), (140, 70, 200), (6 - i) / 6) if i > 1 else (6, 4, 14)
        d.ellipse([fx - rx, fy - ry, fx + rx, fy + ry], fill=c)
    for _ in range(40):
        ang = rnd.random() * 2 * math.pi
        r = rnd.random() ** 0.5
        x, y = int(fx + math.cos(ang) * 30 * r), int(fy + math.sin(ang) * 10 * r)
        if 0 <= y < H:
            px[x, y] = rnd.choice([(255, 255, 255), (180, 220, 255), (220, 160, 255)])
    for k in range(3):  # espiral
        for i in range(60):
            t = i / 60
            ang = t * 5 + k * 2.1
            x = int(fx + math.cos(ang) * (8 + 26 * t))
            y = int(fy + math.sin(ang) * (3 + 10 * t))
            if 0 <= x < W and 0 <= y < H:
                px[x, y] = lerp(px[x, y], (90, 240, 200), 0.6 * (1 - t))
    # correntes penduradas
    for (cx, l) in ((40, 40), (100, 26), (156, 34), (214, 46), (244, 22)):
        for y in range(0, l, 2):
            px[cx, y] = (70, 64, 80)
            if y % 4 == 0:
                px[cx + 1, y + 1] = (54, 50, 64)
                px[cx - 1, y + 1] = (54, 50, 64)
    # braseiros de chama verde
    for bx in (8, 248, 94, 162):
        by = 110
        glow(img, bx, by - 4, 14, (60, 220, 120), 0.45, 6)
        d.rectangle([bx - 2, by - 1, bx + 2, by + 1], fill=(60, 50, 40))
        d.polygon([(bx - 2, by - 1), (bx + 2, by - 1), (bx, by - 7)], fill=(120, 255, 150))
        px[bx, by - 3] = (230, 255, 230)
    # abismo cósmico em baixo
    gradient(img, (0, 118, W, H), [(0, (24, 14, 40)), (0.5, (16, 6, 34)), (1, (36, 12, 64))], steps=8)
    px = img.load()
    for _ in range(70):
        x, y = rnd.randrange(W), rnd.randrange(120, H)
        px[x, y] = rnd.choice([(200, 160, 255), (120, 220, 255), (255, 255, 255)])
    for y in range(110, H):  # névoa roxa a subir
        for x in range(W):
            a = (y - 110) / 33
            if BAYER[y % 4][(x + y // 3) % 4] < a * 0.35 * (0.6 + 0.4 * math.sin(x * 0.07)):
                px[x, y] = lerp(px[x, y], (120, 60, 170), 0.5)
    # tentáculos a emergir do abismo
    d = ImageDraw.Draw(img)
    for (bx, h, curl) in ((70, 36, 1), (182, 44, -1), (128, 24, 1)):
        for i in range(h):
            t = i / h
            x = bx + curl * (math.sin(t * 3.5) * 7 + t * 10)
            w = 4 * (1 - t) + 0.7
            y = H - i
            d.ellipse([x - w, y - 1, x + w, y + 1], fill=(30, 12, 44))
            if i % 4 == 2 and w > 1.6:
                px[int(x - curl * w * 0.5), y] = (110, 60, 140)
    a = np.array(img).astype(float)
    yy, xx = np.mgrid[0:H, 0:W]
    v = np.clip(((xx - W / 2) / (W * 0.6)) ** 2 + ((yy - H * 0.5) / (H * 0.8)) ** 2, 0, 1)
    a *= (1 - 0.5 * v ** 1.5)[..., None]
    return up(Image.fromarray(a.clip(0, 255).astype(np.uint8)), 4)


# ----------------------------------------------------------------------------------
# TEXTURAS DE PLATAFORMA (base 16 px -> x3 = 48 px)
# ----------------------------------------------------------------------------------
def blocos(w, h, base, mortar, rnd, bw=8, bh=4, hl=None):
    t = Image.new("RGBA", (w, h), mortar + (255,))
    d = ImageDraw.Draw(t)
    for r, y in enumerate(range(0, h, bh)):
        off = (r % 2) * (bw // 2)
        for x in range(-off, w, bw):
            s = rnd.randint(-8, 8)
            c = tuple(max(0, min(255, base[i] + s)) for i in range(3))
            d.rectangle([x + 1, y + 1, x + bw - 1, y + bh - 1], fill=c)
            if hl:
                d.line([(x + 1, y + 1), (x + bw - 2, y + 1)], fill=lerp(c, hl, 0.5))
            d.point((x + bw - 1, y + bh - 1), fill=lerp(c, (0, 0, 0), 0.3))
    return t


def tex_cais():
    rnd = random.Random(3)
    corpo = blocos(16, 16, (66, 72, 92), (30, 32, 44), rnd, 8, 4, (100, 110, 130))
    topo = corpo.copy()
    d = ImageDraw.Draw(topo)
    d.rectangle([0, 0, 15, 3], fill=(120, 130, 146))
    d.line([(0, 0), (15, 0)], fill=(170, 186, 196))
    d.line([(0, 3), (15, 3)], fill=(40, 42, 56))
    for x in (3, 11):
        d.point((x, 1), fill=(90, 100, 116))
    return up(topo, 3), up(corpo, 3)


def tex_pedra():
    rnd = random.Random(5)
    corpo = blocos(16, 16, (70, 66, 80), (34, 30, 42), rnd, 8, 5, (96, 92, 110))
    topo = corpo.copy()
    d = ImageDraw.Draw(topo)
    d.rectangle([0, 0, 15, 2], fill=(56, 92, 70))  # musgo
    d.line([(0, 0), (15, 0)], fill=(96, 150, 104))
    for x, l in ((2, 3), (6, 2), (9, 4), (13, 2)):
        d.line([(x, 3), (x, 2 + l)], fill=(56, 92, 70))
    return up(topo, 3), up(corpo, 3)


def tex_madeira():
    t = Image.new("RGBA", (16, 6), (0, 0, 0, 0))
    d = ImageDraw.Draw(t)
    d.rectangle([0, 0, 15, 3], fill=(122, 82, 50))
    d.line([(0, 0), (15, 0)], fill=(170, 122, 78))
    d.line([(0, 2), (15, 2)], fill=(104, 68, 40))
    d.rectangle([0, 4, 15, 5], fill=(64, 40, 26))
    d.line([(15, 0), (15, 3)], fill=(70, 44, 28))
    d.point((2, 1), fill=(200, 200, 190))
    d.point((13, 1), fill=(200, 200, 190))
    d.point((7, 3), fill=(90, 58, 34))
    return up(t, 3)


def tex_caixote():
    t = Image.new("RGBA", (16, 16), (60, 38, 24, 255))
    d = ImageDraw.Draw(t)
    d.rectangle([1, 1, 14, 14], fill=(128, 86, 50))
    for y in (5, 10):
        d.line([(1, y), (14, y)], fill=(100, 66, 38))
    d.line([(2, 2), (13, 13)], fill=(150, 104, 62), width=2)
    d.line([(2, 2), (13, 13)], fill=(92, 60, 34))
    d.rectangle([0, 0, 15, 1], fill=(90, 58, 34))
    d.rectangle([0, 14, 15, 15], fill=(52, 32, 20))
    d.line([(0, 0), (15, 0)], fill=(170, 120, 74))
    for p in ((2, 1), (13, 1), (2, 14), (13, 14)):
        d.point(p, fill=(190, 190, 180))
    return up(t, 3)


def tex_catacumba():
    rnd = random.Random(9)
    corpo = blocos(16, 16, (52, 44, 66), (22, 18, 30), rnd, 8, 4, (76, 66, 94))
    d = ImageDraw.Draw(corpo)
    d.point((5, 6), fill=(80, 170, 140))  # runa ténue
    d.point((5, 7), fill=(60, 120, 100))
    topo = corpo.copy()
    d = ImageDraw.Draw(topo)
    d.rectangle([0, 0, 15, 3], fill=(84, 74, 100))
    d.line([(0, 0), (15, 0)], fill=(128, 116, 146))
    d.line([(0, 3), (15, 3)], fill=(28, 22, 36))
    d.point((4, 1), fill=(60, 52, 76))
    d.point((12, 2), fill=(60, 52, 76))
    return up(topo, 3), up(corpo, 3)


def tex_laje():
    t = Image.new("RGBA", (16, 8), (0, 0, 0, 0))
    d = ImageDraw.Draw(t)
    d.rectangle([0, 0, 15, 5], fill=(74, 66, 92))
    d.line([(0, 0), (15, 0)], fill=(130, 120, 150))
    d.line([(0, 5), (15, 5)], fill=(40, 34, 52))
    d.rectangle([2, 6, 13, 7], fill=(40, 34, 52))
    d.point((7, 7), fill=(170, 110, 255))
    d.point((8, 7), fill=(170, 110, 255))
    d.point((15, 2), fill=(46, 40, 60))
    return up(t, 3)


def tex_runica(on):
    t = Image.new("RGBA", (16, 10), (0, 0, 0, 0))
    d = ImageDraw.Draw(t)
    d.rectangle([0, 0, 15, 9], fill=(58, 60, 74))
    d.line([(0, 0), (15, 0)], fill=(110, 116, 130))
    d.rectangle([0, 8, 15, 9], fill=(34, 34, 46))
    g = (120, 255, 210) if on else (36, 76, 70)
    for p in ((7, 2), (8, 2), (6, 3), (9, 3), (7, 4), (8, 4), (7, 5), (8, 5), (6, 6), (9, 6), (3, 4), (12, 4)):
        d.point(p, fill=g)
    img = up(t, 3)
    if on:
        a = np.array(img).astype(float)
        a[:, :, :3] = np.minimum(255, a[:, :, :3] * 1.1 + np.array([0, 14, 10]))
        img = Image.fromarray(a.astype(np.uint8))
    return img


# ----------------------------------------------------------------------------------
# SPRITES DOS AGENTES (base 18x28 -> x3 = 54x84), virados para a direita
# ----------------------------------------------------------------------------------
PAL = {
    "zig": dict(coat=(170, 124, 72), shade=(126, 88, 50), hat=(78, 58, 44), band=(40, 30, 24), skin=(232, 188, 150),
                pants=(66, 58, 54), shoe=(34, 26, 22), tie=(226, 222, 206), tie2=(96, 30, 30), hair=(96, 62, 36),
                lining=(120, 84, 48), long_hair=False),
    "zag": dict(coat=(64, 66, 82), shade=(40, 42, 54), hat=(32, 32, 40), band=(176, 32, 44), skin=(226, 180, 142),
                pants=(32, 32, 40), shoe=(18, 18, 22), tie=(226, 226, 230), tie2=(190, 34, 48), hair=(44, 30, 26),
                lining=(150, 30, 42), long_hair=True),
}
GLASS = (18, 18, 26)


def draw_agent(p, drop=0, lean=0, legs=((0, 0), (0, 0)), arms=((0, 7), (0, 7)), crouch=False, wings=0, bob=0):
    """legs: ((foot_dx, lift) traseira, (foot_dx, lift) dianteira); arms: ((dx,dy) traseira, (dx,dy) dianteira)"""
    im = Image.new("RGBA", (18, 28), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    oy = drop + bob
    ox = lean
    sh = (8 + ox, 12 + oy)
    # braço traseiro
    bx, by = arms[0]
    if wings == 0:
        d.line([sh, (sh[0] + bx, sh[1] + by)], fill=p["shade"], width=2)
        d.point((sh[0] + bx, sh[1] + by + 1), fill=lerp(p["skin"], (0, 0, 0), 0.25))
    # pernas
    if not crouch:
        for i, (fx, lift) in enumerate(legs):
            hx = 7 + i * 2
            col = p["pants"] if i else lerp(p["pants"], (0, 0, 0), 0.3)
            fy = 26 - lift
            d.line([(hx, 21 + oy), (hx + fx, fy)], fill=col, width=2)
            d.line([(hx + fx, fy + 1), (hx + fx + 2, fy + 1)], fill=p["shoe"])
    else:
        for i, (fx, lift) in enumerate(legs):
            hx = 6 + i * 2 + ox
            col = p["pants"] if i else lerp(p["pants"], (0, 0, 0), 0.3)
            kx = hx + 3 + fx
            d.line([(hx, 24), (kx, 23 - lift)], fill=col, width=2)
            d.line([(kx, 23 - lift), (kx - 1, 26)], fill=col, width=2)
            d.line([(kx - 1, 27), (kx + 1, 27)], fill=p["shoe"])
    # asas do casaco (planar)
    if wings:
        w = wings
        d.polygon([(6 + ox, 12 + oy), (0, 15 + oy - w), (1, 20 + oy), (6 + ox, 19 + oy)], fill=p["coat"])
        d.polygon([(11 + ox, 12 + oy), (17, 15 + oy - w), (16, 20 + oy), (11 + ox, 19 + oy)], fill=p["coat"])
        d.polygon([(6 + ox, 13 + oy), (2, 16 + oy - w), (2, 19 + oy), (6 + ox, 18 + oy)], fill=p["lining"])
        d.polygon([(11 + ox, 13 + oy), (15, 16 + oy - w), (15, 19 + oy), (11 + ox, 18 + oy)], fill=p["lining"])
        d.point((0, 15 + oy - w), fill=p["skin"])
        d.point((17, 15 + oy - w), fill=p["skin"])
    # tronco (gabardina)
    bottom = 19 + oy if not crouch else 24
    d.rectangle([5 + ox, 11 + oy, 12 + ox, bottom], fill=p["coat"])
    d.rectangle([5 + ox, 11 + oy, 6 + ox, bottom], fill=p["shade"])
    d.line([(5 + ox, 16 + oy), (12 + ox, 16 + oy)], fill=p["shade"])  # cinto
    d.point((10 + ox, 16 + oy), fill=(200, 180, 110))
    if not crouch:  # aba da gabardina
        d.polygon([(5 + ox, 20 + oy), (12 + ox, 20 + oy), (13 + ox, 22 + oy), (4 + ox, 22 + oy)], fill=p["coat"])
        d.line([(9 + ox, 20 + oy), (9 + ox, 22 + oy)], fill=p["shade"])
    # gola, camisa e gravata/cachecol
    d.line([(10 + ox, 11 + oy), (10 + ox, 14 + oy)], fill=p["tie"])
    d.line([(11 + ox, 11 + oy), (11 + ox, 13 + oy)], fill=p["tie2"])
    d.point((9 + ox, 11 + oy), fill=p["shade"])
    d.point((12 + ox, 11 + oy), fill=p["shade"])
    # cabeça
    d.rectangle([8 + ox, 10 + oy, 9 + ox, 10 + oy], fill=lerp(p["skin"], (0, 0, 0), 0.2))
    d.rectangle([6 + ox, 5 + oy, 11 + ox, 9 + oy], fill=p["skin"])
    d.rectangle([6 + ox, 5 + oy, 7 + ox, 8 + oy], fill=p["hair"])
    if p["long_hair"]:
        d.rectangle([5 + ox, 5 + oy, 6 + ox, 12 + oy], fill=p["hair"])
    d.line([(8 + ox, 6 + oy), (12 + ox, 6 + oy)], fill=GLASS)
    d.point((11 + ox, 6 + oy), fill=(120, 140, 170))
    d.point((10 + ox, 8 + oy), fill=lerp(p["skin"], (120, 40, 40), 0.4))
    d.point((11 + ox, 9 + oy), fill=lerp(p["skin"], (0, 0, 0), 0.15))
    # chapéu fedora
    d.rectangle([6 + ox, 1 + oy, 11 + ox, 3 + oy], fill=p["hat"])
    d.line([(7 + ox, 1 + oy), (10 + ox, 1 + oy)], fill=lerp(p["hat"], (255, 255, 255), 0.15))
    d.line([(6 + ox, 3 + oy), (11 + ox, 3 + oy)], fill=p["band"])
    d.line([(4 + ox, 4 + oy), (13 + ox, 4 + oy)], fill=p["hat"])
    d.point((8 + ox, 0 + oy), fill=p["hat"]) if oy > 0 else None
    # braço dianteiro
    bx, by = arms[1]
    if wings == 0:
        d.line([(sh[0] + 1, sh[1]), (sh[0] + 1 + bx, sh[1] + by)], fill=p["coat"], width=2)
        d.point((sh[0] + 1 + bx, sh[1] + by + 1), fill=p["skin"])
        d.point((sh[0] + 2 + bx, sh[1] + by + 1), fill=p["skin"])
    return outline(im)


def frames_agent(nome):
    p = PAL[nome]
    fr = {}
    fr["parado"] = [draw_agent(p, legs=((-1, 0), (1, 0)), arms=((-1, 7), (1, 7))),
                    draw_agent(p, legs=((-1, 0), (1, 0)), arms=((-1, 7), (1, 7)), bob=1)]
    front = [3, 1, -2, -3, -1, 2]
    lift = [0, 1, 0, 0, 2, 1]
    bobs = [0, -1, 0, 0, -1, 0]
    fr["correr"] = []
    for i in range(6):
        j = (i + 3) % 6
        fr["correr"].append(draw_agent(p, lean=1, bob=bobs[i] + 1, legs=((front[j], lift[j]), (front[i], lift[i])),
                                       arms=((front[i], 5), (-front[i], 5))))
    fr["saltar"] = [draw_agent(p, legs=((-2, 1), (3, 4)), arms=((-3, -2), (3, -4)))]
    fr["cair"] = [draw_agent(p, legs=((-2, 0), (2, 1)), arms=((-4, 1), (4, 0)))]
    fr["agachado"] = [draw_agent(p, drop=6, lean=1, crouch=True, legs=((0, 0), (1, 0)), arms=((-1, 4), (2, 4)))]
    fr["furtivo"] = []
    for (a, b) in ((1, -1), (0, 0), (-1, 1), (0, 0)):
        fr["furtivo"].append(draw_agent(p, drop=6, lean=1, crouch=True, legs=((a, 0), (b, 1 if b > 0 else 0)),
                                        arms=((a - 1, 4), (b + 2, 4))))
    if nome == "zag":
        fr["planar"] = [draw_agent(p, wings=1, legs=((-1, 0), (1, 0))), draw_agent(p, wings=-1, legs=((-1, 0), (1, 0)))]
    return {k: [up(f, 3) for f in v] for k, v in fr.items()}


if __name__ == "__main__":
    fundo_docas().save(f"{OUT}/fundo_nivel1_docas.png")
    fundo_templo().save(f"{OUT}/fundo_nivel2_templo.png")
    fundo_docas(escuro=0.35).save(f"{OUT}/fundo_menu.png")
    a, b = tex_cais(); a.save(f"{OUT}/cais_topo.png"); b.save(f"{OUT}/cais.png")
    a, b = tex_pedra(); a.save(f"{OUT}/pedra_topo.png"); b.save(f"{OUT}/pedra.png")
    a, b = tex_catacumba(); a.save(f"{OUT}/catacumba_topo.png"); b.save(f"{OUT}/catacumba.png")
    tex_madeira().save(f"{OUT}/madeira.png")
    tex_caixote().save(f"{OUT}/caixote.png")
    tex_laje().save(f"{OUT}/laje.png")
    tex_runica(False).save(f"{OUT}/runica_off.png")
    tex_runica(True).save(f"{OUT}/runica_on.png")
    for nome in ("zig", "zag"):
        for estado, lst in frames_agent(nome).items():
            for i, f in enumerate(lst):
                f.save(f"{OUT}/{nome}_{estado}_{i}.png")
    print("ok", len(os.listdir(OUT)))
