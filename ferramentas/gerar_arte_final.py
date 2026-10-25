"""Arte dos níveis 3-5 (Biblioteca, Observatório, Fossa) e variantes de inimigos.
Uso: python3 gerar_arte_final.py <pasta_images>"""
import math, random, os, sys
from PIL import Image, ImageDraw
import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gerar_imagens import BAYER, up, lerp, dith, gradient, glow, outline, blocos
from gerar_arte_missao import cultista, texto, PW, PH

OUT = sys.argv[1] if len(sys.argv) > 1 else "images"
os.makedirs(OUT, exist_ok=True)


def save(img, nome):
    img.save(os.path.join(OUT, nome))


def vinheta(img, forca=0.45):
    a = np.array(img).astype(float)
    h, w = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w]
    v = np.clip(((yy - h * 0.45) / (h * 0.85)) ** 2, 0, 1)
    a *= (1 - forca * v ** 1.5)[..., None]
    return Image.fromarray(a.clip(0, 255).astype(np.uint8))


# ======================================================================= NÍVEL 3 — BIBLIOTECA
def panorama_biblioteca():
    rnd = random.Random(31)
    img = Image.new("RGB", (PW, PH))
    gradient(img, (0, 0, PW, PH), [(0, (30, 20, 18)), (0.5, (42, 28, 22)), (1, (22, 14, 12))], steps=8)
    d = ImageDraw.Draw(img)
    cores_livros = [(110, 30, 34), (40, 60, 90), (60, 80, 50), (120, 90, 40), (80, 50, 90), (140, 120, 90), (50, 40, 36)]
    for jx in range(30, PW - 40, 120):                # janelas góticas com luar
        d.rectangle([jx, 22, jx + 30, 96], fill=(26, 34, 58))
        d.ellipse([jx, 8, jx + 30, 38], fill=(26, 34, 58))
        gradient(img, (jx + 2, 24, jx + 29, 95), [(0, (70, 90, 140)), (1, (30, 40, 70))], steps=6)
        d = ImageDraw.Draw(img)
        d.ellipse([jx + 2, 10, jx + 28, 36], fill=(70, 90, 140))
        d.line([(jx + 15, 10), (jx + 15, 96)], fill=(24, 18, 16), width=2)
        for y in (40, 60, 80):
            d.line([(jx, y), (jx + 30, y)], fill=(24, 18, 16))
        d.ellipse([jx + 9, 16, jx + 21, 28], outline=(24, 18, 16))
        px = img.load()
        for y in range(96, 135):                   # feixe de luar
            for x in range(jx - 6, jx + 50):
                t = (y - 96) / 40
                if jx + t * 18 - 2 < x < jx + 32 + t * 18 and 0 <= x < PW and BAYER[y % 4][x % 4] < 0.25 * (1 - t):
                    px[x, y] = lerp(px[x, y], (120, 140, 190), 0.35)
    d = ImageDraw.Draw(img)
    for sx in range(70, PW, 120):                    # estantes do chão ao teto
        x0, x1 = sx, sx + 74
        d.rectangle([x0, 4, x1, 132], fill=(46, 30, 20))
        d.rectangle([x0, 4, x0 + 2, 132], fill=(70, 46, 30))
        for ry in range(8, 128, 10):
            d.line([(x0, ry + 8), (x1, ry + 8)], fill=(70, 46, 30))
            x = x0 + 4
            while x < x1 - 3:
                w = rnd.randint(2, 4)
                h = rnd.randint(5, 8)
                c = rnd.choice(cores_livros)
                if rnd.random() < 0.015:
                    c = (90, 220, 170)               # livro que brilha
                d.rectangle([x, ry + 8 - h, x + w - 1, ry + 7], fill=c)
                d.point((x, ry + 8 - h), fill=lerp(c, (255, 255, 255), 0.3))
                x += w + (1 if rnd.random() < 0.2 else 0)
    d.rectangle([0, 58, PW, 60], fill=(60, 40, 26))      # varandim
    for x in range(0, PW, 6):
        d.line([(x, 52), (x, 58)], fill=(60, 40, 26))
    d.line([(0, 52), (PW, 52)], fill=(80, 56, 36))
    for cx in range(120, PW, 180):                  # candelabros
        d.line([(cx, 0), (cx, 18)], fill=(80, 70, 50))
        d.line([(cx - 10, 20), (cx + 10, 20)], fill=(150, 120, 60), width=2)
        for k in (-10, -4, 4, 10):
            glow(img, cx + k, 16, 10, (255, 200, 120), 0.35, 6)
        d = ImageDraw.Draw(img)
        for k in (-10, -4, 4, 10):
            d.rectangle([cx + k, 16, cx + k, 19], fill=(240, 230, 200))
            d.point((cx + k, 15), fill=(255, 220, 120))
    for bx in range(40, PW, 95):                    # candeeiros verdes de leitura
        glow(img, bx, 116, 16, (120, 230, 140), 0.35, 6)
        d = ImageDraw.Draw(img)
        d.rectangle([bx - 14, 122, bx + 14, 125], fill=(60, 36, 22))
        d.polygon([(bx - 5, 114), (bx + 5, 114), (bx + 3, 118), (bx - 3, 118)], fill=(40, 140, 80))
        d.line([(bx, 118), (bx, 122)], fill=(170, 150, 80))
    # estandarte com olho
    d.rectangle([250, 64, 270, 96], fill=(90, 20, 30))
    d.polygon([(250, 96), (260, 102), (270, 96)], fill=(90, 20, 30))
    d.ellipse([254, 74, 266, 82], fill=(200, 190, 140))
    d.ellipse([258, 75, 262, 81], fill=(30, 10, 10))
    gradient(img, (0, 132, PW, PH), [(0, (50, 32, 22)), (1, (24, 14, 10))], steps=6)
    return up(vinheta(img), 4)


def tex_soalho():
    rnd = random.Random(41)
    t = Image.new("RGBA", (16, 16), (40, 24, 16, 255))
    d = ImageDraw.Draw(t)
    for r, y in enumerate(range(0, 16, 4)):
        off = (r % 2) * 6
        for x in range(-off, 16, 12):
            c = (98 + rnd.randint(-8, 8), 62 + rnd.randint(-6, 6), 38)
            d.rectangle([x, y, x + 11, y + 2], fill=c)
            d.line([(x, y), (x + 10, y)], fill=lerp(c, (255, 220, 170), 0.25))
    corpo = t.copy()
    d.rectangle([0, 0, 15, 2], fill=(140, 96, 60))
    d.line([(0, 0), (15, 0)], fill=(190, 140, 90))
    d.line([(0, 3), (15, 3)], fill=(30, 18, 12))
    return up(t, 3), up(corpo, 3)


def tex_estante():
    rnd = random.Random(43)
    cores = [(110, 30, 34), (40, 60, 90), (60, 80, 50), (120, 90, 40), (80, 50, 90), (140, 120, 90)]
    t = Image.new("RGBA", (16, 16), (46, 30, 20, 255))
    d = ImageDraw.Draw(t)
    for ry in (0, 8):
        d.line([(0, ry + 7), (15, ry + 7)], fill=(80, 54, 34))
        x = 1
        while x < 15:
            w = rnd.randint(1, 3)
            h = rnd.randint(4, 6)
            d.rectangle([x, ry + 7 - h, min(14, x + w - 1), ry + 6], fill=rnd.choice(cores))
            x += w
    d.line([(0, 0), (0, 15)], fill=(70, 46, 30))
    topo = t.copy()
    d = ImageDraw.Draw(topo)
    d.rectangle([0, 0, 15, 2], fill=(110, 74, 46))
    d.line([(0, 0), (15, 0)], fill=(160, 112, 70))
    return up(topo, 3), up(t, 3)


def carrinho_livros():
    rnd = random.Random(47)
    im = Image.new("RGBA", (32, 26), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([1, 8, 30, 21], fill=(92, 58, 34))
    d.rectangle([1, 8, 30, 9], fill=(130, 88, 52))
    d.line([(1, 15), (30, 15)], fill=(70, 44, 26))
    cores = [(110, 30, 34), (40, 60, 90), (60, 80, 50), (120, 90, 40), (80, 50, 90)]
    x = 3
    while x < 29:
        w = rnd.randint(2, 3)
        h = rnd.randint(4, 7)
        d.rectangle([x, 8 - h, x + w - 1, 7], fill=rnd.choice(cores))
        x += w
    for wx in (5, 26):
        d.ellipse([wx - 3, 20, wx + 3, 25], fill=(40, 40, 46))
        d.point((wx, 22), fill=(150, 150, 160))
    d.rectangle([4, 11, 27, 13], fill=(70, 44, 26))
    return outline(im)


# ======================================================================= NÍVEL 4 — OBSERVATÓRIO
def panorama_observatorio():
    rnd = random.Random(53)
    img = Image.new("RGB", (PW, PH))
    gradient(img, (0, 0, PW, 110), [(0, (6, 8, 22)), (0.6, (18, 22, 46)), (1, (40, 46, 70))], steps=12)
    px = img.load()
    for _ in range(320):
        x, y = rnd.randrange(PW), rnd.randrange(100)
        px[x, y] = rnd.choice([(200, 210, 230), (150, 160, 200), (230, 220, 180)])
    for k, (col, y0) in enumerate((((90, 230, 160), 18), ((170, 90, 220), 30))):   # aurora estranha
        for x in range(PW):
            yy = y0 + int(8 * math.sin(x * 0.02 + k * 2) + 4 * math.sin(x * 0.07))
            for t in range(14):
                y = yy + t
                if 0 <= y < PH and BAYER[y % 4][x % 4] < 0.35 * (1 - t / 14):
                    px[x, y] = lerp(px[x, y], col, 0.45)
    for i in range(7):                               # as sete estrelas alinhadas
        sx, sy = 300 + i * 22, 14 + int(abs(i - 3) * 3)
        glow(img, sx, sy, 9, (255, 120, 120), 0.5, 6)
        px = img.load()
        px[sx, sy] = (255, 240, 240)
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            px[sx + dx, sy + dy] = (255, 160, 160)
    d = ImageDraw.Draw(img)
    def serra(base, alt, cor, neve, seed, passo):
        r = random.Random(seed)
        pts = [(0, PH)]
        x = 0
        while x <= PW + passo:
            pts.append((x, base - r.randint(0, alt)))
            x += passo
        pts.append((PW, PH))
        d.polygon(pts, fill=cor)
        for i in range(1, len(pts) - 2):
            (x1, y1), (x2, y2) = pts[i], pts[i + 1]
            if y1 < base - alt * 0.5:
                d.polygon([(x1, y1), (x1 - 5, y1 + 7), (x1 + 6, y1 + 6)], fill=neve)
    serra(84, 40, (34, 40, 62), (170, 180, 200), 1, 30)
    serra(100, 30, (24, 28, 44), (140, 150, 175), 2, 24)
    # observatório (direita)
    d.rectangle([330, 70, 470, 132], fill=(60, 62, 76))
    for y in range(72, 132, 6):
        d.line([(330, y), (470, y)], fill=(52, 54, 66))
    d.ellipse([350, 34, 450, 106], fill=(170, 176, 190))
    d.ellipse([350, 34, 400, 106], fill=(200, 206, 218))
    d.rectangle([346, 70, 454, 74], fill=(90, 92, 106))
    d.polygon([(398, 36), (404, 36), (412, 70), (390, 70)], fill=(20, 22, 34))   # fenda da cúpula
    d.line([(401, 60), (430, 20)], fill=(110, 112, 126), width=3)              # telescópio
    for wx in range(340, 470, 22):
        d.rectangle([wx, 88, wx + 8, 100], fill=(220, 200, 120) if rnd.random() < 0.6 else (40, 44, 60))
    d.line([(486, 132), (486, 30)], fill=(70, 72, 84), width=2)                 # antena
    for y in range(36, 130, 10):
        d.line([(482, y), (490, y + 8)], fill=(70, 72, 84))
    glow(img, 486, 30, 8, (255, 60, 60), 0.6, 6)
    d = ImageDraw.Draw(img)
    d.point((486, 30), fill=(255, 200, 200))
    gradient(img, (0, 116, PW, PH), [(0, (40, 46, 70)), (0.4, (16, 18, 32)), (1, (4, 4, 10))], steps=8)   # precipício
    px = img.load()
    for y in range(116, 132):                         # névoa no precipício
        for x in range(PW):
            if BAYER[y % 4][x % 4] < 0.3 * (1 - (y - 116) / 16):
                px[x, y] = lerp(px[x, y], (150, 160, 190), 0.4)
    for _ in range(500):                              # neve a cair
        x, y = rnd.randrange(PW), rnd.randrange(PH)
        px[x, y] = lerp(px[x, y], (230, 236, 250), 0.7)
    return up(vinheta(img, 0.35), 4)


def tex_neve():
    rnd = random.Random(59)
    corpo = blocos(16, 16, (66, 70, 88), (34, 36, 48), rnd, 8, 5, (96, 100, 120))
    topo = corpo.copy()
    d = ImageDraw.Draw(topo)
    d.rectangle([0, 0, 15, 3], fill=(220, 226, 240))
    d.line([(0, 0), (15, 0)], fill=(255, 255, 255))
    for x, l in ((1, 2), (5, 3), (9, 1), (12, 3)):
        d.line([(x, 4), (x, 3 + l)], fill=(200, 206, 224))
    return up(topo, 3), up(corpo, 3)


def tex_fragil():
    t = Image.new("RGBA", (16, 10), (0, 0, 0, 0))
    d = ImageDraw.Draw(t)
    d.rectangle([0, 0, 15, 7], fill=(150, 156, 176))
    d.line([(0, 0), (15, 0)], fill=(230, 236, 250))
    d.rectangle([0, 6, 15, 7], fill=(90, 96, 116))
    d.line([(4, 1), (6, 4), (5, 7)], fill=(60, 64, 80))
    d.line([(11, 0), (10, 3), (12, 6)], fill=(60, 64, 80))
    d.point((8, 8), fill=(120, 126, 146)); d.point((2, 9), fill=(120, 126, 146))
    return up(t, 3)


def tex_elevador(aceso):
    t = Image.new("RGBA", (16, 10), (0, 0, 0, 0))
    d = ImageDraw.Draw(t)
    d.rectangle([0, 0, 15, 9], fill=(84, 88, 100))
    d.line([(0, 0), (15, 0)], fill=(150, 156, 170))
    d.rectangle([0, 8, 15, 9], fill=(50, 52, 62))
    for x in range(1, 16, 3):
        d.point((x, 4), fill=(60, 62, 72))
    lamp = (255, 210, 90) if aceso else (90, 70, 40)
    d.rectangle([6, 2, 9, 5], fill=lamp)
    return up(t, 3)


# ======================================================================= NÍVEL 5 — FOSSA
def panorama_fossa():
    rnd = random.Random(61)
    img = Image.new("RGB", (PW, PH))
    gradient(img, (0, 0, PW, PH), [(0, (14, 40, 58)), (0.35, (8, 22, 40)), (1, (4, 8, 20))], steps=12)
    px = img.load()
    for k in range(6):                                 # raios de luz da superfície
        x0 = 40 + k * 85
        for y in range(0, 90):
            for x in range(x0 + y // 3, x0 + y // 3 + 10):
                if 0 <= x < PW and BAYER[y % 4][x % 4] < 0.25 * (1 - y / 90):
                    px[x, y] = lerp(px[x, y], (120, 200, 210), 0.3)
    d = ImageDraw.Draw(img)
    # olho gigante
    ex, ey = 360, 58
    glow(img, ex, ey, 90, (140, 30, 60), 0.45, 8)
    d = ImageDraw.Draw(img)
    d.ellipse([ex - 60, ey - 22, ex + 60, ey + 22], fill=(40, 10, 24))
    d.ellipse([ex - 54, ey - 18, ex + 54, ey + 18], fill=(210, 190, 150))
    d.ellipse([ex - 18, ey - 18, ex + 18, ey + 18], fill=(170, 40, 50))
    d.ellipse([ex - 4, ey - 16, ex + 4, ey + 16], fill=(10, 4, 8))
    d.point((ex - 8, ey - 8), fill=(255, 240, 240))
    for k in range(-50, 51, 10):                        # pálpebra / pestanas-tentáculo
        d.line([(ex + k, ey - 21), (ex + k * 1.3, ey - 34 - abs(k) // 6)], fill=(30, 8, 20), width=2)
    for (bx, curl) in ((250, 1), (300, -1), (420, 1), (470, -1)):   # tentáculos à volta
        for i in range(60):
            t = i / 60
            x = bx + curl * (math.sin(t * 4) * 12 + t * 20)
            y = PH - i * 1.6
            w = 5 * (1 - t) + 1
            d.ellipse([x - w, y - 1, x + w, y + 1], fill=(36, 12, 30))
    # paredes da caverna com estalactites
    for x in range(0, PW, 9):
        h = rnd.randint(6, 28)
        d.polygon([(x, 0), (x + 9, 0), (x + 5, h)], fill=(10, 18, 28))
    d.polygon([(0, 0), (60, 0), (40, 60), (20, 110), (0, 143)], fill=(10, 20, 30))
    d.polygon([(PW, 0), (PW - 50, 0), (PW - 30, 70), (PW - 10, 143), (PW, 143)], fill=(10, 20, 30))
    # costelas gigantes
    for i, x in enumerate(range(90, 230, 30)):
        d.arc([x - 20, 20, x + 30, 160], 200, 340, fill=(140, 150, 140), width=3)
    px = img.load()
    for _ in range(260):                              # bioluminescência
        x, y = rnd.randrange(PW), rnd.randrange(PH)
        c = rnd.choice([(90, 255, 220), (140, 120, 255), (255, 140, 200)])
        px[x, y] = c
        if rnd.random() < 0.2:
            glow(img, x, y, 4, c, 0.4, 4)
            px = img.load()
    gradient(img, (0, 128, PW, PH), [(0, (14, 30, 40)), (1, (4, 10, 18))], steps=6)
    return up(vinheta(img, 0.3), 4)


def tex_rocha_abissal():
    rnd = random.Random(67)
    corpo = blocos(16, 16, (30, 40, 56), (14, 18, 28), rnd, 8, 5, (50, 64, 84))
    d = ImageDraw.Draw(corpo)
    for (x, y) in ((3, 6), (11, 12), (7, 2)):
        d.point((x, y), fill=(90, 255, 220))
    topo = corpo.copy()
    d = ImageDraw.Draw(topo)
    d.rectangle([0, 0, 15, 2], fill=(40, 90, 90))
    d.line([(0, 0), (15, 0)], fill=(90, 220, 200))
    for x in (2, 6, 13):
        d.line([(x, 3), (x, 5)], fill=(40, 90, 90))
    return up(topo, 3), up(corpo, 3)


def tex_cristal():
    t = Image.new("RGBA", (16, 8), (0, 0, 0, 0))
    d = ImageDraw.Draw(t)
    d.polygon([(0, 2), (3, 0), (12, 0), (15, 2), (13, 5), (2, 5)], fill=(80, 200, 220))
    d.line([(3, 0), (12, 0)], fill=(200, 255, 255))
    d.polygon([(2, 5), (13, 5), (9, 7), (6, 7)], fill=(40, 110, 150))
    d.line([(5, 1), (7, 4)], fill=(170, 250, 255))
    return up(t, 3)


# ======================================================================= inimigos novos
VIGIA = dict(robe=(70, 22, 26), robe2=(46, 14, 18), hem=(120, 46, 40), face=(8, 6, 6), eye=(255, 170, 60),
             sigil=(230, 190, 90), rope=(150, 120, 60), skin=(160, 140, 120), crown=None)
SACER = dict(robe=(26, 22, 30), robe2=(14, 12, 18), hem=(180, 30, 40), face=(210, 204, 186), eye=(255, 40, 40),
             sigil=(220, 40, 50), rope=(200, 170, 80), skin=(150, 130, 140), crown=(230, 200, 90))

if __name__ == "__main__":
    save(panorama_biblioteca(), "panorama_nivel3.png")
    save(panorama_observatorio(), "panorama_nivel4.png")
    save(panorama_fossa(), "panorama_nivel5.png")
    a, b = tex_soalho(); save(a, "soalho_topo.png"); save(b, "soalho.png")
    a, b = tex_estante(); save(a, "estante_topo.png"); save(b, "estante.png")
    a, b = tex_neve(); save(a, "neve_topo.png"); save(b, "neve.png")
    a, b = tex_rocha_abissal(); save(a, "rocha_abissal_topo.png"); save(b, "rocha_abissal.png")
    save(tex_cristal(), "cristal.png")
    save(tex_fragil(), "fragil.png")
    save(tex_elevador(False), "elevador_off.png")
    save(tex_elevador(True), "elevador_on.png")
    save(up(carrinho_livros(), 3), "carrinho_livros.png")
    for (p, n) in ((VIGIA, "vigia"), (SACER, "sacerdote")):
        for i in range(4):
            save(up(cultista(p, "andar", i), 3), f"{n}_andar_{i}.png")
        save(up(cultista(p, "parado"), 3), f"{n}_parado_0.png")
        save(up(cultista(p, "alerta", olhos=(255, 60, 60)), 3), f"{n}_alerta_0.png")
        save(up(cultista(p, "atordoado", olhos=(120, 120, 120)), 3), f"{n}_atordoado_0.png")
        save(up(cultista(p, "neutralizado"), 3), f"{n}_neutralizado_0.png")
    for i in range(2):
        save(up(cultista(SACER, "invocar", i, olhos=(255, 60, 60)), 3), f"sacerdote_invocar_{i}.png")
    print("ok")
