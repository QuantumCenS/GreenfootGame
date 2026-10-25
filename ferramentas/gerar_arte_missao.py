"""Arte da missão (Membro 2 + níveis grandes): inimigos, objetos, ícones e panoramas.
Usa as funções base de gerar_imagens.py.  Uso:  python3 gerar_arte_missao.py <pasta_images>"""
import math, random, os, sys
from PIL import Image, ImageDraw
import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gerar_imagens import (BAYER, up, lerp, dith, gradient, glow, outline, blocos, house_row,
                           draw_agent, PAL)

OUT = sys.argv[1] if len(sys.argv) > 1 else "images"
os.makedirs(OUT, exist_ok=True)
INK = (14, 12, 22, 255)


def save(img, name):
    img.save(os.path.join(OUT, name))


# ---------------------------------------------------------------- fonte 3x5 (letreiros)
FONT = {
    'A': ["010", "101", "111", "101", "101"], 'T': ["111", "010", "010", "010", "010"],
    'L': ["100", "100", "100", "100", "111"], 'N': ["110", "101", "101", "101", "101"], 'Q': ["010", "101", "101", "111", "011"],
    'I': ["111", "010", "010", "010", "111"], 'D': ["110", "101", "101", "101", "110"],
    'C': ["011", "100", "100", "100", "011"], 'O': ["010", "101", "101", "101", "010"],
    'S': ["011", "100", "010", "001", "110"], 'E': ["111", "100", "110", "100", "111"],
    'R': ["110", "101", "110", "101", "101"], 'V': ["101", "101", "101", "101", "010"],
    'U': ["101", "101", "101", "101", "111"], 'F': ["111", "100", "110", "100", "100"],
    'X': ["101", "101", "010", "101", "101"], 'Í': ["010", "000", "010", "010", "010"],
    'P': ["110", "101", "110", "100", "100"], 'M': ["101", "111", "111", "101", "101"],
    '4': ["101", "101", "111", "001", "001"], ' ': ["000"] * 5, '-': ["000", "000", "111", "000", "000"],
}


def texto(d, x, y, s, col):
    for ch in s:
        g = FONT.get(ch, FONT[' '])
        for r, row in enumerate(g):
            for c, v in enumerate(row):
                if v == '1':
                    d.point((x + c, y + r), fill=col)
        x += 4
    return x


# ================================================================ INIMIGOS
CULT = dict(robe=(30, 44, 44), robe2=(20, 30, 32), hem=(44, 62, 60), face=(8, 10, 12), eye=(120, 255, 210),
            sigil=(150, 220, 170), rope=(110, 90, 60), skin=(150, 160, 140), crown=None)
INVO = dict(robe=(64, 30, 82), robe2=(44, 20, 58), hem=(180, 140, 60), face=(10, 6, 14), eye=(230, 120, 255),
            sigil=(240, 200, 90), rope=(180, 140, 60), skin=(140, 120, 150), crown=(200, 170, 90))


def cultista(p, pose="andar", fase=0, olhos=None):
    im = Image.new("RGBA", (28, 28), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    eye = olhos or p["eye"]
    if pose == "neutralizado":
        d.polygon([(3, 27), (3, 22), (8, 20), (22, 21), (25, 24), (25, 27)], fill=p["robe"])
        d.line([(4, 27), (24, 27)], fill=p["robe2"])
        d.ellipse([21, 20, 27, 26], fill=p["robe2"])        # capuz caído
        d.point((24, 23), fill=(60, 70, 70))
        d.line([(10, 22), (18, 22)], fill=p["rope"])
        d.line([(6, 25), (2, 26)], fill=p["skin"])
        return outline(im)
    sway = [0, 1, 0, -1][fase % 4] if pose == "andar" else 0
    cx = 13
    # pés
    if pose == "andar":
        a = [(-3, 0), (-1, 0), (2, 0), (0, 0)][fase % 4]
        d.line([(cx + a[0] - 1, 27), (cx + a[0] + 1, 27)], fill=(30, 24, 20))
        b = [(2, 0), (0, 0), (-3, 0), (-1, 0)][fase % 4]
        d.line([(cx + b[0] - 1, 27), (cx + b[0] + 1, 27)], fill=(40, 32, 26))
    else:
        d.line([(cx - 4, 27), (cx - 2, 27)], fill=(30, 24, 20))
        d.line([(cx + 2, 27), (cx + 4, 27)], fill=(40, 32, 26))
    # túnica
    d.polygon([(cx - 4, 9), (cx + 4, 9), (cx + 7 + sway, 26), (cx - 7 + sway, 26)], fill=p["robe"])
    d.polygon([(cx - 4, 9), (cx - 2, 9), (cx - 5 + sway, 26), (cx - 7 + sway, 26)], fill=p["robe2"])
    for x in range(cx - 7 + sway, cx + 8 + sway, 2):        # bainha esfarrapada
        d.point((x, 26), fill=p["hem"])
    d.line([(cx - 4, 17), (cx + 5, 17)], fill=p["rope"])
    d.point((cx + 4, 18), fill=p["rope"])
    d.point((cx + 4, 19), fill=p["rope"])
    # sigilo (olho com tentáculos)
    sx, sy = cx + 1, 12
    for q in ((0, 0), (1, 0), (-1, 1), (2, 1), (0, 2), (1, 2)):
        d.point((sx + q[0], sy + q[1]), fill=p["sigil"])
    d.point((sx, sy + 1), fill=p["face"])
    d.point((sx + 1, sy + 1), fill=eye)
    # capuz
    d.ellipse([cx - 5, 1, cx + 5, 11], fill=p["robe"])
    d.polygon([(cx - 5, 6), (cx - 2, 0), (cx + 2, 0), (cx + 5, 6)], fill=p["robe"])
    d.ellipse([cx - 1, 3, cx + 5, 10], fill=p["face"])
    d.point((cx + 2, 6), fill=eye)
    d.point((cx + 4, 6), fill=eye)
    d.point((cx - 4, 4), fill=p["robe2"])
    if p["crown"]:
        for i, x in enumerate(range(cx - 4, cx + 5, 2)):
            d.line([(x, 1), (x + (1 if i % 2 else -1), -1 if i == 2 else 0)], fill=p["crown"])
        d.line([(cx - 4, 2), (cx + 4, 2)], fill=p["crown"])
    # braços
    if pose == "alerta":
        d.line([(cx + 2, 11), (cx + 9, 9)], fill=p["robe"], width=2)
        d.point((cx + 10, 9), fill=p["skin"])
    elif pose == "invocar":
        d.line([(cx - 2, 11), (cx - 5, 4)], fill=p["robe"], width=2)
        d.line([(cx + 3, 11), (cx + 7, 4)], fill=p["robe"], width=2)
        r = 2 + fase
        glow_col = (200, 120, 255)
        d.ellipse([cx + 7 - r, 1 - r // 2, cx + 7 + r, 1 + r], fill=glow_col)
        d.point((cx + 7, 2), fill=(255, 230, 255))
    elif pose == "atordoado":
        d.line([(cx - 3, 11), (cx - 3, 4)], fill=p["robe"], width=2)
        d.line([(cx + 3, 11), (cx + 4, 4)], fill=p["robe"], width=2)
    else:
        sw = [2, 1, -1, 0][fase % 4] if pose == "andar" else 0
        d.line([(cx + 1, 11), (cx + 2 + sw, 18)], fill=p["robe2"], width=2)
        d.point((cx + 2 + sw, 19), fill=p["skin"])
    return outline(im)


CRIA = dict(body=(46, 26, 60), dark=(26, 14, 36), belly=(80, 50, 92), spot=(110, 255, 210), mouth=(140, 30, 50),
            teeth=(230, 220, 200), leg=(36, 20, 48))


def criatura(fase=0, pose="rastejar"):
    im = Image.new("RGBA", (32, 22), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    p = CRIA
    flat = 2 if pose == "atordoado" else 0
    bob = [0, 1, 0, 1][fase % 4] if pose != "atordoado" else 0
    # tentáculos-pernas
    for i, lx in enumerate((6, 11, 16, 21, 25)):
        ph = (fase + i) % 4
        dx = [-2, -1, 1, 2][ph] if pose != "atordoado" else 3
        d.line([(lx, 15 + bob + flat), (lx + dx, 19), (lx + dx - 1, 21)], fill=p["leg"], width=1)
        d.point((lx + dx - 2, 21), fill=p["leg"])
    # corpo
    d.ellipse([2, 4 + bob + flat, 28, 18 + bob], fill=p["body"])
    d.ellipse([4, 11 + bob + flat, 26, 18 + bob], fill=p["belly"])
    d.ellipse([2, 4 + bob + flat, 14, 12 + bob + flat], fill=p["dark"])
    # espinhos dorsais
    for x in (6, 10, 14, 18):
        d.polygon([(x, 6 + bob + flat), (x + 2, 6 + bob + flat), (x + 1, 2 + bob + flat)], fill=p["dark"])
    # manchas bioluminescentes
    spot = p["spot"] if pose != "atordoado" else (60, 120, 100)
    for (x, y) in ((8, 8), (12, 7), (17, 8), (21, 10), (10, 11)):
        d.point((x, y + bob + flat), fill=spot)
    # boca vertical (frente = direita)
    if pose == "cacar":
        d.ellipse([23, 5 + bob, 31, 17 + bob], fill=p["mouth"])
        for y in range(7, 16, 2):
            d.point((24, y + bob), fill=p["teeth"])
            d.point((30, y + bob + 1), fill=p["teeth"])
        d.ellipse([26, 9 + bob, 28, 13 + bob], fill=(40, 6, 16))
    else:
        d.line([(27, 7 + bob + flat), (28, 15 + bob)], fill=p["mouth"])
        d.point((26, 9 + bob + flat), fill=p["teeth"])
        d.point((26, 12 + bob), fill=p["teeth"])
    # tentáculos faciais
    tl = 2 if pose == "cacar" else 0
    d.line([(28, 8 + bob), (31, 6 + bob - tl)], fill=p["leg"])
    d.line([(28, 14 + bob), (31, 16 + bob + tl)], fill=p["leg"])
    return outline(im)


# ================================================================ OBJETOS
def terminal(estado):
    im = Image.new("RGBA", (20, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([3, 15, 16, 23], fill=(54, 56, 66))           # pedestal
    d.rectangle([3, 15, 16, 16], fill=(84, 88, 100))
    d.rectangle([5, 19, 14, 20], fill=(36, 38, 46))
    d.rectangle([1, 13, 18, 14], fill=(70, 72, 84))           # teclado
    for x in range(2, 18, 2):
        d.point((x, 13), fill=(110, 114, 128))
    d.rectangle([2, 1, 17, 12], fill=(64, 66, 78))            # monitor CRT
    d.rectangle([2, 1, 17, 2], fill=(92, 96, 110))
    scr = {"ativo": ((10, 40, 24), (90, 255, 150)), "hackeado": ((10, 26, 46), (120, 200, 255)),
           "erro": ((46, 8, 10), (255, 80, 80))}[estado]
    d.rectangle([4, 3, 15, 10], fill=scr[0])
    if estado == "ativo":
        for y, l in ((4, 8), (6, 5), (8, 9)):
            d.line([(5, y), (5 + l, y)], fill=scr[1])
    elif estado == "hackeado":
        d.line([(6, 7), (8, 9)], fill=scr[1]); d.line([(8, 9), (13, 4)], fill=scr[1])
    else:
        d.line([(7, 4), (12, 9)], fill=scr[1]); d.line([(12, 4), (7, 9)], fill=scr[1])
    d.point((16, 11), fill=scr[1])
    return outline(im)


def nota():
    im = Image.new("RGBA", (8, 10), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.polygon([(0, 1), (6, 0), (7, 8), (1, 9)], fill=(222, 206, 160))
    d.line([(1, 1), (6, 0)], fill=(240, 228, 190))
    for y in (2, 4, 6):
        d.line([(2, y), (5, y - 0)], fill=(120, 96, 70))
    d.point((6, 8), fill=(170, 150, 110))
    d.point((1, 8), fill=(150, 40, 40))
    return outline(im)


def ficheiro():
    im = Image.new("RGBA", (12, 10), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 1, 11, 9], fill=(196, 160, 96))
    d.rectangle([0, 0, 4, 1], fill=(196, 160, 96))
    d.line([(0, 1), (11, 1)], fill=(220, 186, 120))
    d.rectangle([2, 4, 9, 6], fill=(170, 30, 36))             # carimbo CLASSIFICADO
    d.line([(3, 5), (8, 5)], fill=(240, 200, 200))
    d.point((10, 8), fill=(150, 120, 70))
    return outline(im)


def frasco():
    im = Image.new("RGBA", (5, 8), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([2, 0, 2, 1], fill=(130, 90, 60))
    d.rectangle([1, 2, 3, 7], fill=(80, 170, 230))
    d.rectangle([1, 5, 3, 7], fill=(140, 230, 255))
    d.point((1, 3), fill=(220, 250, 255))
    return outline(im)


def amuleto():
    im = Image.new("RGBA", (7, 7), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse([0, 0, 6, 6], fill=(170, 140, 60))
    d.ellipse([1, 1, 5, 5], fill=(200, 255, 140))
    d.point((3, 3), fill=(20, 60, 30))
    d.point((2, 2), fill=(255, 255, 230))
    return outline(im)


def caixa_pesada():
    rnd = random.Random(21)
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 31, 31], fill=(84, 60, 40))
    for y in range(2, 30, 5):
        c = (118 + rnd.randint(-8, 8), 82, 50)
        d.rectangle([2, y, 29, y + 3], fill=c)
        d.line([(2, y), (29, y)], fill=lerp(c, (255, 220, 160), 0.25))
    for x in (0, 30):                                         # cintas de ferro
        d.rectangle([x, 0, x + 1, 31], fill=(70, 74, 84))
    for y in (0, 15, 30):
        d.rectangle([0, y, 31, y + 1], fill=(70, 74, 84))
        for x in (3, 15, 27):
            d.point((x, y), fill=(170, 176, 186))
    d.rectangle([9, 6, 22, 12], fill=(40, 30, 22))           # carimbo com peixe
    d.polygon([(11, 9), (15, 7), (19, 9), (15, 11)], fill=(190, 180, 150))
    d.polygon([(19, 9), (21, 7), (21, 11)], fill=(190, 180, 150))
    d.point((13, 9), fill=(40, 30, 22))
    return outline(im)


def estatua():
    im = Image.new("RGBA", (32, 38), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    st, st2, st3 = (92, 88, 104), (64, 60, 76), (124, 120, 138)
    d.rectangle([2, 30, 29, 37], fill=st2)                   # pedestal
    d.rectangle([2, 30, 29, 31], fill=st3)
    d.rectangle([5, 33, 26, 35], fill=(48, 44, 58))
    d.ellipse([5, 9, 26, 31], fill=st)                       # corpo agachado
    d.ellipse([5, 9, 13, 30], fill=st2)
    d.ellipse([8, 0, 23, 15], fill=st)                       # cabeça
    d.ellipse([8, 0, 13, 14], fill=st2)
    d.point((17, 6), fill=(110, 255, 200))
    d.point((20, 6), fill=(110, 255, 200))
    for i, x in enumerate((11, 14, 17, 20, 23)):             # tentáculos faciais
        d.line([(x, 12), (x + (i % 2) - 1, 19 + i % 3)], fill=st2)
    d.polygon([(3, 16), (6, 12), (8, 22)], fill=st2)          # asas atrofiadas
    d.polygon([(28, 16), (25, 12), (23, 22)], fill=st)
    d.line([(14, 26), (18, 23), (22, 26)], fill=st3)
    return outline(im)


def placa_pressao(premida):
    im = Image.new("RGBA", (32, 4), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 2, 31, 3], fill=(44, 44, 54))
    top = 2 if premida else 0
    col = (90, 220, 140) if premida else (200, 150, 60)
    d.rectangle([2, top, 29, 2], fill=(110, 110, 124))
    d.line([(2, top), (29, top)], fill=(160, 160, 176))
    d.line([(13, top + (1 if not premida else 0)), (18, top + (1 if not premida else 0))], fill=col)
    return im


def tex_metal():
    t = Image.new("RGBA", (16, 16), (52, 58, 70, 255))
    d = ImageDraw.Draw(t)
    d.rectangle([1, 1, 14, 14], fill=(74, 82, 96))
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        d.point((x, y), fill=(140, 150, 166))
    for i in range(3, 13, 3):
        d.line([(i, 5), (i + 2, 5)], fill=(90, 98, 114))
        d.line([(i + 1, 10), (i + 3, 10)], fill=(90, 98, 114))
    corpo = t.copy()
    d.rectangle([0, 0, 15, 2], fill=(116, 126, 142))
    d.line([(0, 0), (15, 0)], fill=(176, 186, 200))
    d.line([(0, 3), (15, 3)], fill=(40, 44, 56))
    for x in range(1, 16, 4):
        d.point((x, 1), fill=(220, 180, 60))          # faixa de aviso
        d.point((x + 1, 1), fill=(30, 30, 36))
    return up(t, 3), up(corpo, 3)


def tex_grade():
    t = Image.new("RGBA", (16, 6), (0, 0, 0, 0))
    d = ImageDraw.Draw(t)
    d.rectangle([0, 0, 15, 1], fill=(120, 128, 140))
    d.line([(0, 0), (15, 0)], fill=(170, 178, 190))
    for x in range(0, 16, 3):
        d.line([(x, 2), (x + 1, 4)], fill=(80, 86, 98))
    d.line([(0, 5), (15, 5)], fill=(60, 64, 76))
    return up(t, 3)


def tex_portao_metal():
    t = Image.new("RGBA", (16, 16), (60, 64, 74, 255))
    d = ImageDraw.Draw(t)
    for y in range(0, 16, 4):
        d.rectangle([0, y, 15, y + 2], fill=(98, 104, 116))
        d.line([(0, y), (15, y)], fill=(136, 142, 156))
    d.line([(0, 0), (0, 15)], fill=(40, 42, 50))
    d.line([(15, 0), (15, 15)], fill=(40, 42, 50))
    return up(t, 3)


def tex_portao_pedra():
    t = Image.new("RGBA", (16, 16), (40, 34, 52, 255))
    d = ImageDraw.Draw(t)
    d.rectangle([1, 0, 14, 15], fill=(70, 62, 86))
    d.line([(1, 0), (1, 15)], fill=(96, 88, 114))
    for (x, y) in ((7, 4), (8, 5), (6, 6), (9, 6), (7, 8), (8, 9), (7, 11)):
        d.point((x, y), fill=(120, 80, 190))
    return up(t, 3)


def cofre():
    im = Image.new("RGBA", (16, 53), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 52], fill=(70, 76, 90))
    d.rectangle([1, 1, 14, 51], fill=(104, 112, 128))
    d.line([(1, 1), (1, 51)], fill=(140, 148, 164))
    for y in (6, 46):
        d.rectangle([2, y, 13, y + 1], fill=(64, 70, 82))
    d.ellipse([3, 19, 12, 28], fill=(70, 76, 90))           # roda do cofre
    d.ellipse([5, 21, 10, 26], fill=(150, 156, 170))
    d.line([(7, 18), (7, 29)], fill=(64, 70, 82))
    d.line([(2, 23), (13, 23)], fill=(64, 70, 82))
    d.rectangle([5, 33, 10, 36], fill=(200, 40, 40))         # luz de bloqueio
    d.rectangle([3, 40, 12, 41], fill=(220, 180, 60))
    return up(im, 3)


def placa_aviso():
    im = Image.new("RGBA", (12, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([5, 6, 6, 15], fill=(92, 66, 44))
    d.rectangle([0, 0, 11, 7], fill=(132, 96, 60))
    d.rectangle([1, 1, 10, 6], fill=(170, 128, 80))
    d.line([(2, 2), (9, 2)], fill=(80, 56, 36))
    d.line([(2, 4), (7, 4)], fill=(80, 56, 36))
    d.point((10, 6), fill=(110, 80, 50))
    return outline(im)


def sino():
    im = Image.new("RGBA", (12, 30), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    for y in range(0, 15, 2):
        d.point((6, y), fill=(90, 84, 96))
        d.point((5, y + 1), fill=(70, 66, 76))
    d.rectangle([5, 15, 6, 16], fill=(120, 100, 60))
    d.polygon([(3, 17), (8, 17), (10, 26), (1, 26)], fill=(160, 130, 60))
    d.polygon([(3, 17), (5, 17), (3, 26), (1, 26)], fill=(200, 170, 90))
    d.rectangle([0, 26, 11, 27], fill=(120, 96, 44))
    d.point((5, 28), fill=(90, 70, 40))
    for (x, y) in ((5, 20), (6, 22)):
        d.point((x, y), fill=(110, 255, 200))
    return outline(im)


def coracao(cheio):
    m = ["0110110", "1111111", "1111111", "0111110", "0011100", "0001000"]
    im = Image.new("RGBA", (8, 7), (0, 0, 0, 0))
    for y, row in enumerate(m):
        for x, v in enumerate(row):
            if v == '1':
                im.putpixel((x, y), (200, 40, 70, 255) if cheio else (60, 50, 70, 255))
    if cheio:
        im.putpixel((1, 1), (255, 160, 180, 255))
    return outline(im)


def olho_sanidade():
    im = Image.new("RGBA", (9, 7), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse([0, 1, 8, 5], fill=(220, 220, 200))
    d.ellipse([3, 1, 5, 5], fill=(40, 140, 110))
    d.point((4, 3), fill=(0, 0, 0))
    return outline(im)


# ================================================================ PANORAMAS (2048 x 572)
PW, PH, HZ = 512, 143, 92


def panorama_docas():
    rnd = random.Random(17)
    img = Image.new("RGB", (PW, PH))
    gradient(img, (0, 0, PW, HZ), [(0, (10, 10, 30)), (0.55, (26, 30, 62)), (1, (52, 74, 88))], steps=12)
    px = img.load()
    for _ in range(280):
        x, y = rnd.randrange(PW), rnd.randrange(HZ - 20)
        px[x, y] = rnd.choice([(200, 210, 230), (150, 160, 200), (230, 220, 180), (120, 140, 190)])
    mx, my, mr = 150, 28, 16
    glow(img, mx, my, 60, (90, 140, 120), 0.35, 8)
    glow(img, mx, my, 30, (150, 200, 170), 0.5, 8)
    d = ImageDraw.Draw(img)
    d.ellipse([mx - mr, my - mr, mx + mr, my + mr], fill=(214, 232, 200))
    for (cx, cy, r) in ((-6, -4, 4), (5, 3, 5), (-2, 8, 3), (8, -8, 2)):
        d.ellipse([mx + cx - r, my + cy - r, mx + cx + r, my + cy + r], fill=(184, 206, 176))
    # farol e promontório
    d.polygon([(0, 60), (14, 58), (30, 64), (46, 72), (62, 82), (78, HZ), (0, HZ)], fill=(22, 26, 40))
    d.rectangle([16, 40, 21, 58], fill=(46, 48, 64))
    d.rectangle([16, 44, 21, 45], fill=(120, 40, 46)); d.rectangle([16, 50, 21, 51], fill=(120, 40, 46))
    d.rectangle([15, 36, 22, 40], fill=(30, 30, 40)); d.rectangle([17, 37, 20, 39], fill=(255, 236, 160))
    # vila
    house_row(d, rnd, 90, 270, HZ - 14, 10, 18, (34, 38, 58), (58, 34, 40), (230, 190, 100), lit=0.25)
    house_row(d, rnd, 110, 270, HZ - 4, 8, 14, (42, 46, 68), (74, 40, 44), (250, 210, 120), lit=0.3)
    d.rectangle([180, 50, 188, HZ - 14], fill=(36, 40, 60))
    d.polygon([(179, 50), (189, 50), (184, 40)], fill=(58, 34, 40))
    # grua do porto
    d.line([(236, HZ), (236, 34)], fill=(28, 30, 44), width=3)
    d.line([(200, 36), (262, 36)], fill=(28, 30, 44), width=2)
    for x in range(204, 260, 6):
        d.line([(x, 36), (x + 3, 39)], fill=(28, 30, 44))
    d.line([(214, 37), (214, 60)], fill=(40, 42, 56))
    d.rectangle([210, 60, 218, 66], fill=(110, 46, 40))
    # mar
    gradient(img, (0, HZ, 270, PH), [(0, (30, 52, 70)), (0.4, (18, 34, 52)), (1, (8, 16, 30))], steps=10)
    px = img.load()
    for _ in range(260):
        x, y = rnd.randrange(270), rnd.randrange(HZ + 2, PH)
        for i in range(rnd.randint(2, 6)):
            if x + i < 270:
                px[x + i, y] = lerp(px[x + i, y], (60, 96, 110), 0.6)
    for y in range(HZ + 1, PH, 2):
        spread = 6 + (y - HZ) * 0.35
        for _ in range(3):
            x = int(mx + rnd.uniform(-spread, spread))
            for i in range(rnd.randint(2, 7)):
                if 0 <= x + i < 270:
                    px[x + i, y] = lerp(px[x + i, y], (190, 220, 180), 0.7 - (y - HZ) / 120)
    d = ImageDraw.Draw(img)
    for (bx, h, curl, s) in ((60, 26, 1, 3), (74, 34, -1, 4), (88, 20, 1, 3)):
        for i in range(h):
            t = i / h
            x = bx + curl * (math.sin(t * 3.2) * 6 + t * t * 8)
            w = max(1, s * (1 - t) + 0.6)
            d.ellipse([x - w, HZ + 4 - i - 1, x + w, HZ + 4 - i + 1], fill=(14, 22, 30))
    # ---------------- Conserveira Atlântida (lado direito)
    bx0 = 270
    # telhado em dente de serra
    for x in range(bx0, PW, 24):
        d.polygon([(x, 30), (x + 18, 16), (x + 18, 30)], fill=(40, 30, 34))
        d.polygon([(x + 18, 16), (x + 24, 16), (x + 24, 30), (x + 18, 30)], fill=(70, 90, 100))
        d.line([(x + 19, 18), (x + 23, 18)], fill=(120, 170, 160))
    # chaminé com fumo esverdeado
    d.rectangle([300, 0, 310, 30], fill=(70, 40, 36))
    for y in range(2, 30, 4):
        d.line([(300, y), (310, y)], fill=(54, 30, 28))
    for i in range(40):
        x = 305 + int(math.sin(i * 0.3) * (2 + i * 0.2)) - i // 3
        y = -i // 2 + 2
        if 0 <= y < PH:
            glow(img, x, max(0, y), 3 + i // 8, (70, 110, 90), 0.3, 4)
    d = ImageDraw.Draw(img)
    # paredes de tijolo
    for row, y in enumerate(range(30, PH, 4)):
        off = (row % 2) * 4
        for x in range(bx0 - off, PW, 8):
            s = rnd.randint(-6, 6)
            c = (92 + s, 48 + s // 2, 42 + s // 2)
            d.rectangle([x + 1, y + 1, x + 7, y + 3], fill=c)
    gradient_mask = np.linspace(0.75, 0.25, PH - 30)
    a = np.array(img).astype(float)
    a[30:, bx0:, :] *= gradient_mask[:, None, None]
    img = Image.fromarray(a.clip(0, 255).astype(np.uint8))
    d = ImageDraw.Draw(img)
    px = img.load()
    # janelas altas
    for x in range(bx0 + 10, PW - 6, 22):
        d.rectangle([x, 40, x + 10, 64], fill=(24, 22, 30))
        lit = rnd.random() < 0.6
        col = (210, 220, 120) if lit else (40, 60, 64)
        for gy in range(41, 64, 6):
            for gx in range(x + 1, x + 10, 3):
                d.rectangle([gx, gy, gx + 1, gy + 4], fill=col if rnd.random() < 0.85 else (30, 34, 40))
        if lit:
            glow(img, x + 5, 52, 12, (190, 200, 110), 0.18, 6)
            d = ImageDraw.Draw(img)
    # letreiro
    d.rectangle([330, 72, 478, 84], fill=(22, 20, 26))
    d.rectangle([330, 72, 478, 72], fill=(90, 80, 70))
    texto(d, 336, 75, "CONSERVEIRA", (200, 220, 190))
    texto(d, 384 + 2, 75, "", (0, 0, 0))
    texto(d, 384 + 2 + 4, 75, "ATLANTIDA", (120, 255, 200))
    # portas de carga e tanque 4
    for x in (290, 420):
        d.rectangle([x, 94, x + 30, PH], fill=(40, 42, 50))
        for y in range(96, PH, 4):
            d.line([(x + 1, y), (x + 29, y)], fill=(60, 64, 74))
    d.rectangle([360, 96, 404, PH], fill=(30, 40, 44))
    texto(d, 368, 100, "TANQUE 4", (200, 60, 60))
    for y in range(108, PH, 6):
        d.line([(361, y), (403, y)], fill=(44, 60, 64))
    # vinheta
    a = np.array(img).astype(float)
    yy, xx = np.mgrid[0:PH, 0:PW]
    v = np.clip(((yy - PH * 0.45) / (PH * 0.85)) ** 2, 0, 1)
    a *= (1 - 0.45 * v ** 1.5)[..., None]
    return up(Image.fromarray(a.clip(0, 255).astype(np.uint8)), 4)


def panorama_templo():
    rnd = random.Random(29)
    img = Image.new("RGB", (PW, PH))
    gradient(img, (0, 0, PW, PH), [(0, (12, 10, 22)), (0.6, (22, 18, 36)), (1, (14, 10, 26))], steps=8)
    d = ImageDraw.Draw(img)
    px = img.load()
    for row, y in enumerate(range(0, 118, 7)):
        off = (row % 2) * 7
        for x in range(-off, PW, 14):
            s = rnd.randint(-4, 5)
            c = (30 + s, 26 + s, 44 + s)
            d.rectangle([x + 1, y + 1, x + 13, y + 6], fill=c)
            d.line([(x + 1, y + 1), (x + 12, y + 1)], fill=lerp(c, (60, 54, 80), 0.4))
    for ax in range(28, PW, 64):
        d.rectangle([ax - 12, 46, ax + 12, 118], fill=(10, 8, 18))
        d.ellipse([ax - 12, 34, ax + 12, 58], fill=(10, 8, 18))
        d.ellipse([ax - 4, 62, ax + 4, 72], fill=(26, 22, 36))
        d.polygon([(ax - 7, 118), (ax + 7, 118), (ax + 5, 70), (ax - 5, 70)], fill=(26, 22, 36))
        px[ax - 1, 67] = (90, 230, 170); px[ax + 1, 67] = (90, 230, 170)
    for cx in range(60, PW, 64):
        d.rectangle([cx - 7, 20, cx + 7, 120], fill=(40, 34, 56))
        d.rectangle([cx - 7, 20, cx - 5, 120], fill=(56, 50, 74))
        d.rectangle([cx + 5, 20, cx + 7, 120], fill=(28, 24, 40))
        d.rectangle([cx - 9, 16, cx + 9, 21], fill=(50, 44, 66))
        d.rectangle([cx - 9, 114, cx + 9, 120], fill=(50, 44, 66))
        for i, ry in enumerate(range(34, 104, 14)):
            glow(img, cx, ry + 1.5, 6, (60, 200, 160), 0.35, 6)
            for q in ((cx - 1, ry), (cx + 1, ry), (cx, ry + 1), (cx, ry + 2), (cx - 1, ry + 3), (cx + 1, ry + 3)):
                px[q] = (130, 255, 200)
    # fenda cósmica (perto do fim do nível)
    fx, fy = 430, 14
    glow(img, fx, fy, 90, (90, 40, 140), 0.6, 8)
    d = ImageDraw.Draw(img)
    for i in range(6, 0, -1):
        rx, ry = 44 * i / 6, 15 * i / 6
        c = lerp((20, 6, 40), (140, 70, 200), (6 - i) / 6) if i > 1 else (6, 4, 14)
        d.ellipse([fx - rx, fy - ry, fx + rx, fy + ry], fill=c)
    px = img.load()
    for _ in range(60):
        ang, r = rnd.random() * 2 * math.pi, rnd.random() ** 0.5
        x, y = int(fx + math.cos(ang) * 40 * r), int(fy + math.sin(ang) * 12 * r)
        if 0 <= y < PH and 0 <= x < PW:
            px[x, y] = rnd.choice([(255, 255, 255), (180, 220, 255), (220, 160, 255)])
    for k in range(3):
        for i in range(70):
            t = i / 70
            ang = t * 5 + k * 2.1
            x, y = int(fx + math.cos(ang) * (8 + 34 * t)), int(fy + math.sin(ang) * (3 + 12 * t))
            if 0 <= x < PW and 0 <= y < PH:
                px[x, y] = lerp(px[x, y], (90, 240, 200), 0.6 * (1 - t))
    # correntes
    for cx in range(40, PW, 57):
        l = rnd.randint(18, 46)
        for y in range(0, l, 2):
            px[cx, y] = (70, 64, 80)
    # braseiros
    d = ImageDraw.Draw(img)
    for bx in range(8, PW, 77):
        glow(img, bx, 106, 14, (60, 220, 120), 0.45, 6)
        d = ImageDraw.Draw(img)
        d.rectangle([bx - 2, 109, bx + 2, 111], fill=(60, 50, 40))
        d.polygon([(bx - 2, 109), (bx + 2, 109), (bx, 103)], fill=(120, 255, 150))
    # abismo
    gradient(img, (0, 118, PW, PH), [(0, (24, 14, 40)), (0.5, (16, 6, 34)), (1, (36, 12, 64))], steps=8)
    px = img.load()
    for _ in range(140):
        x, y = rnd.randrange(PW), rnd.randrange(120, PH)
        px[x, y] = rnd.choice([(200, 160, 255), (120, 220, 255), (255, 255, 255)])
    for y in range(110, PH):
        for x in range(PW):
            a = (y - 110) / 33
            if BAYER[y % 4][(x + y // 3) % 4] < a * 0.35 * (0.6 + 0.4 * math.sin(x * 0.07)):
                px[x, y] = lerp(px[x, y], (120, 60, 170), 0.5)
    d = ImageDraw.Draw(img)
    for (bx, h, curl) in ((70, 36, 1), (182, 44, -1), (300, 30, 1), (400, 50, -1), (470, 28, 1)):
        for i in range(h):
            t = i / h
            x = bx + curl * (math.sin(t * 3.5) * 7 + t * 10)
            w = 4 * (1 - t) + 0.7
            d.ellipse([x - w, PH - i - 1, x + w, PH - i + 1], fill=(30, 12, 44))
    a = np.array(img).astype(float)
    yy, xx = np.mgrid[0:PH, 0:PW]
    v = np.clip(((yy - PH * 0.5) / (PH * 0.8)) ** 2, 0, 1)
    a *= (1 - 0.5 * v ** 1.5)[..., None]
    return up(Image.fromarray(a.clip(0, 255).astype(np.uint8)), 4)


# ================================================================ poses novas dos agentes
def poses_agentes():
    for nome in ("zig", "zag"):
        p = PAL[nome]
        lanc = draw_agent(p, legs=((-2, 0), (2, 0)), arms=((-4, -3), (6, -3)))
        save(up(lanc, 3), f"{nome}_lancar_0.png")
    p = PAL["zag"]  # Zag a escrever no terminal
    for i, (a, b) in enumerate(((4, 3), (5, 2))):
        save(up(draw_agent(p, legs=((-1, 0), (1, 0)), arms=((a - 1, b + 1), (a, b))), 3), f"zag_hackear_{i}.png")
    p = PAL["zig"]  # golpe furtivo do Zig
    golpe = [((-3, -6), (-2, -7)), ((2, 0), (7, 1)), ((1, 5), (3, 5))]
    for i, (ra, fa) in enumerate(golpe):
        img = draw_agent(p, drop=2 if i == 1 else 0, lean=1 if i == 1 else 0,
                         legs=((-2, 0), (3, 0)), arms=(ra, fa))
        save(up(img, 3), f"zig_atacar_{i}.png")


if __name__ == "__main__":
    for i in range(4):
        save(up(cultista(CULT, "andar", i), 3), f"cultista_andar_{i}.png")
        save(up(cultista(INVO, "andar", i), 3), f"invocador_andar_{i}.png")
        save(up(criatura(i), 3), f"criatura_rastejar_{i}.png")
    for (p, n) in ((CULT, "cultista"), (INVO, "invocador")):
        save(up(cultista(p, "parado"), 3), f"{n}_parado_0.png")
        save(up(cultista(p, "alerta", olhos=(255, 60, 60)), 3), f"{n}_alerta_0.png")
        save(up(cultista(p, "atordoado", olhos=(120, 120, 120)), 3), f"{n}_atordoado_0.png")
        save(up(cultista(p, "neutralizado"), 3), f"{n}_neutralizado_0.png")
    for i in range(2):
        save(up(cultista(INVO, "invocar", i, olhos=(255, 60, 60)), 3), f"invocador_invocar_{i}.png")
        save(up(criatura(i * 2, "cacar"), 3), f"criatura_cacar_{i}.png")
    save(up(criatura(0, "atordoado"), 3), "criatura_atordoado_0.png")
    for e in ("ativo", "hackeado", "erro"):
        save(up(terminal(e), 3), f"terminal_{e}.png")
    save(up(nota(), 3), "nota.png")
    save(up(ficheiro(), 3), "ficheiro_secreto.png")
    save(up(frasco(), 3), "frasco_eter.png")
    save(up(amuleto(), 3), "amuleto.png")
    save(up(caixa_pesada(), 3), "caixa_pesada.png")
    save(up(estatua(), 3), "estatua.png")
    save(up(placa_pressao(False), 3), "placa_solta.png")
    save(up(placa_pressao(True), 3), "placa_premida.png")
    a, b = tex_metal(); save(a, "metal_topo.png"); save(b, "metal.png")
    save(tex_grade(), "grade.png")
    save(tex_portao_metal(), "portao_metal.png")
    save(tex_portao_pedra(), "portao_pedra.png")
    save(cofre(), "cofre.png")
    save(up(placa_aviso(), 3), "placa_aviso.png")
    save(up(sino(), 3), "sino.png")
    save(up(coracao(True), 3), "coracao_cheio.png")
    save(up(coracao(False), 3), "coracao_vazio.png")
    save(up(olho_sanidade(), 3), "icone_sanidade.png")
    save(up(nota(), 2), "icone_nota.png")
    save(up(ficheiro(), 2), "icone_ficheiro.png")
    save(panorama_docas(), "panorama_nivel1.png")
    save(panorama_templo(), "panorama_nivel2.png")
    poses_agentes()
    print("ok")
