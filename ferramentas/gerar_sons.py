"""Gera todos os sons do jogo por síntese (sem ficheiros de terceiros).
WAV PCM 16 bits, mono, 22050 Hz.  Uso: python3 gerar_sons.py <pasta_sounds>"""
import os, sys, wave
import numpy as np

OUT = sys.argv[1] if len(sys.argv) > 1 else "sounds"
os.makedirs(OUT, exist_ok=True)
SR = 22050
rng = np.random.default_rng(7)


def t(d):
    return np.arange(int(SR * d)) / SR


def env(n, a=0.01, r=0.1, curva=1.0):
    e = np.ones(n)
    na, nr = int(SR * a), int(SR * r)
    if na: e[:na] = np.linspace(0, 1, na)
    if nr: e[-nr:] *= np.linspace(1, 0, nr) ** curva
    return e


def dec(d, k):
    return np.exp(-k * t(d))


def seno(f, d, fase=0):
    return np.sin(2 * np.pi * f * t(d) + fase)


def varrimento(f0, f1, d):
    f = np.linspace(f0, f1, int(SR * d))
    return np.sin(2 * np.pi * np.cumsum(f) / SR)


def ruido(d):
    return rng.uniform(-1, 1, int(SR * d))


def passa_baixo(x, corte):
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / SR)
    X *= 1 / (1 + (f / corte) ** 4)
    return np.fft.irfft(X, len(x))


def passa_banda(x, centro, largura):
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / SR)
    X *= np.exp(-((f - centro) / largura) ** 2)
    return np.fft.irfft(X, len(x))


def gravar(nome, x, volume=0.6):
    x = np.asarray(x, dtype=float)
    pico = np.max(np.abs(x)) or 1
    x = x / pico * volume
    dados = (np.clip(x, -1, 1) * 32767).astype(np.int16)
    with wave.open(os.path.join(OUT, nome), "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes(dados.tobytes())


def sino(f, d, k=1.6):
    parciais = [(1, 1), (2.76, 0.5), (5.4, 0.25), (8.9, 0.12), (0.5, 0.3)]
    return sum(a * seno(f * m, d) * dec(d, k * (1 + m * 0.3)) for m, a in parciais)


# ------------------------------------------------------------------ efeitos
gravar("salto.wav", varrimento(260, 620, 0.12) * env(int(SR * .12), .005, .06), 0.35)
gravar("lancar.wav", passa_banda(ruido(0.22), 1800, 900) * np.sin(np.linspace(0, np.pi, int(SR * .22))), 0.4)
g = passa_baixo(ruido(0.25), 300) * dec(0.25, 18) + 0.8 * seno(70, 0.25) * dec(0.25, 14)
gravar("golpe.wav", g, 0.7)
x = passa_banda(ruido(0.5), 6000, 3000) * dec(0.5, 9)
for k in range(8):
    f = rng.uniform(2500, 6000); ini = int(rng.uniform(0, 0.15) * SR)
    s = seno(f, 0.3) * dec(0.3, 18); x[ini:ini + len(s)] += 0.4 * s[:len(x) - ini]
gravar("frasco.wav", x, 0.55)
d = 0.9
gravar("amuleto.wav", sum(seno(f, d) for f in (1320, 1660, 1980, 2640)) * (0.6 + 0.4 * seno(9, d)) * env(int(SR * d), .05, .6), 0.35)
gravar("sino.wav", sino(196, 3.0), 0.7)
d = 0.45
gravar("dano.wav", (seno(110, d) + seno(116.5, d) + 0.5 * seno(233, d)) * dec(d, 7) + 0.5 * passa_baixo(ruido(d), 600) * dec(d, 20), 0.6)
gravar("queda.wav", varrimento(900, 120, 0.7) * env(int(SR * .7), .01, .2), 0.4)
d = 0.45
saw = lambda f: 2 * ((t(d) * f) % 1) - 1
gravar("alerta.wav", passa_baixo(np.concatenate([saw(196)[:int(SR * .15)], saw(207.6)[:int(SR * .3)]]), 1400) * env(int(SR * .45), .01, .15), 0.5)
d = 0.7
gravar("cria.wav", passa_baixo(ruido(d), 260) * (0.6 + 0.4 * seno(23, d)) * env(int(SR * d), .05, .3) + 0.4 * varrimento(90, 60, d) * env(int(SR * d), .05, .3), 0.6)
d = 0.55
gravar("orbe.wav", varrimento(500, 180, d) * (0.5 + 0.5 * seno(14, d)) * env(int(SR * d), .02, .2), 0.4)
d = 1.4
f = 600 + 250 * np.sin(2 * np.pi * 1.5 * t(d))
gravar("alarme.wav", np.sign(np.sin(2 * np.pi * np.cumsum(f) / SR)) * 0.6 * env(int(SR * d), .01, .1), 0.3)
bip = lambda f, dd: np.sign(seno(f, dd)) * env(int(SR * dd), .002, .02)
gravar("terminal.wav", np.concatenate([bip(880, .08), np.zeros(int(SR * .04)), bip(1320, .1)]), 0.25)
gravar("tecla.wav", passa_banda(ruido(0.035), 3000, 1500) * dec(0.035, 90), 0.4)
gravar("hack_ok.wav", np.concatenate([bip(f, .09) for f in (523, 659, 784, 1046)]), 0.25)
gravar("hack_erro.wav", np.concatenate([bip(f, .16) for f in (330, 247, 165)]), 0.3)
d = 0.8
gravar("nota.wav", 0.6 * passa_banda(ruido(d), 2500, 1500) * dec(d, 12) + sum(seno(f, d) * dec(d, 4) for f in (880, 1108, 1318)) * 0.4, 0.45)
d = 1.3
stamp = passa_baixo(ruido(d), 500) * dec(d, 25)
gravar("ficheiro.wav", stamp + 0.5 * sum(seno(f, d) * env(int(SR * d), .15, .6) for f in (220, 261.6, 329.6, 415.3)), 0.6)
d = 1.1
gravar("portao.wav", passa_baixo(ruido(d), 180) * env(int(SR * d), .1, .3) + 0.3 * seno(48, d) * env(int(SR * d), .1, .3), 0.6)
gravar("placa.wav", np.concatenate([passa_banda(ruido(.04), 2000, 800) * dec(.04, 60), passa_baixo(ruido(.12), 400) * dec(.12, 30)]), 0.5)
d = 0.35
gravar("rachar.wav", passa_banda(ruido(d), 1200, 900) * (rng.uniform(0, 1, int(SR * d)) > 0.7) * dec(d, 8), 0.5)
d = 2.2
gravar("nivel.wav", sum(seno(f, d) * env(int(SR * d), 0.05 + i * 0.25, .8) for i, f in enumerate((196, 246.9, 293.7, 392))), 0.5)
d = 3.0
cluster = sum(varrimento(f, f * 0.5, d) for f in (220, 233, 247, 262)) * env(int(SR * d), .05, 1.2)
gravar("insanidade.wav", cluster + 0.6 * passa_baixo(ruido(d), 120) * env(int(SR * d), .5, 1.5), 0.55)
d = 4.0
acordes = [(130.8, 155.6, 196), (138.6, 174.6, 207.7), (130.8, 164.8, 196, 261.6)]
partes = []
for i, ac in enumerate(acordes):
    dd = 1.3 if i < 2 else 1.4
    partes.append(sum(seno(f, dd) + 0.3 * seno(2 * f, dd) for f in ac) * env(int(SR * dd), .08, .3))
gravar("vitoria.wav", np.concatenate(partes) + 0.4 * np.concatenate([sino(523, 4.0)[:sum(len(p) for p in partes)]]), 0.5)
gravar("menu_mover.wav", seno(660, .05) * dec(.05, 60), 0.3)
gravar("menu_ok.wav", (seno(523, .3) + seno(784, .3)) * dec(.3, 9), 0.35)

# ------------------------------------------------------------------ músicas em loop (10 s, periódicas)
D = 10.0
def periodico(f):   # arredonda a frequência para repetir exatamente em D segundos
    return round(f * D) / D

def drone(fs, d=D, lfo=0.1):
    return sum(seno(periodico(f), d) * (0.6 + 0.4 * np.sin(2 * np.pi * lfo * (i + 1) * t(d) + i)) for i, f in enumerate(fs))

def ruido_periodico(centro, largura, d=D):
    return passa_banda(ruido(d), centro, largura)   # filtrado por FFT => circular, logo repete sem corte

m = drone((55, 82.4, 65.4, 110)) * 0.6
for ini in (0.5, 5.5):
    s = sino(196, 4.0, 1.0); i0 = int(ini * SR); m[i0:i0 + len(s)] += 0.25 * s[:len(m) - i0]
gravar("musica_menu.wav", m, 0.45)

ondas = ruido_periodico(500, 400) * (0.55 + 0.45 * np.sin(2 * np.pi * 0.2 * t(D)))
gravar("ambiente_1.wav", ondas + 0.5 * drone((49, 73.4)), 0.4)

coro = sum(seno(periodico(f), D) * (1 + 0.004 * seno(5, D)) for f in (110, 130.8, 164.8, 220))
gravar("ambiente_2.wav", drone((41.2, 61.7)) + 0.35 * coro * (0.6 + 0.4 * np.sin(2 * np.pi * 0.1 * t(D))), 0.4)

tic = np.zeros(int(SR * D))
for k in range(10):
    s = passa_banda(ruido(.03), 3500 if k % 2 else 2500, 800) * dec(.03, 120)
    i0 = int(k * SR); tic[i0:i0 + len(s)] += s
gravar("ambiente_3.wav", tic * 0.8 + drone((55, 58.3, 82.4)) * 0.5, 0.4)

vento = ruido_periodico(700, 500) * (0.4 + 0.6 * np.abs(np.sin(2 * np.pi * 0.15 * t(D))))
gravar("ambiente_4.wav", vento + 0.25 * drone((880, 932.3), lfo=0.2) + 0.4 * drone((46.2, 69.3)), 0.4)

bat = np.zeros(int(SR * D))
for k in range(8):
    for off, a in ((0, 1.0), (0.22, 0.7)):
        s = seno(48, .25) * dec(.25, 16); i0 = int((k * 1.25 + off) * SR); bat[i0:i0 + len(s)] += a * s[:len(bat) - i0]
baleia = seno(periodico(140), D) * (0.5 + 0.5 * np.sin(2 * np.pi * 0.1 * t(D)))
gravar("ambiente_5.wav", bat + 0.4 * passa_baixo(ruido(D), 90) + 0.25 * baleia + 0.4 * drone((36.7, 55)), 0.5)
print("ok", len(os.listdir(OUT)))
