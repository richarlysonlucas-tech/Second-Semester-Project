const socket = new WebSocket("ws://127.0.0.1:8080");

const config = {
    type: Phaser.AUTO,
    width: 800,
    height: 600,
    parent: 'game-container',
    pixelArt: true, 
    scene: { preload, create, update }
};

const game = new Phaser.Game(config);

let jogadores = {}; 
let jogadorPrincipal; 
let teclas;

function preload() {
    this.load.spritesheet('base_personagem', 'assets/personagem.png', { 
        frameWidth: 32, 
        frameHeight: 32 
    });
}

function create() {
    this.anims.create({ key: 'andar-baixo', frames: this.anims.generateFrameNumbers('base_personagem', { start: 0, end: 3 }), frameRate: 8, repeat: -1 });
    this.anims.create({ key: 'andar-esquerda', frames: this.anims.generateFrameNumbers('base_personagem', { start: 4, end: 7 }), frameRate: 8, repeat: -1 });
    this.anims.create({ key: 'andar-direita', frames: this.anims.generateFrameNumbers('base_personagem', { start: 8, end: 11 }), frameRate: 8, repeat: -1 });
    this.anims.create({ key: 'andar-cima', frames: this.anims.generateFrameNumbers('base_personagem', { start: 12, end: 15 }), frameRate: 8, repeat: -1 });

    jogadorPrincipal = this.add.sprite(400, 300, 'base_personagem');
    teclas = this.input.keyboard.addKeys('W,A,S,D');
    
    // Ouve o clique do mouse esquerdo para atacar
    this.input.on('pointerdown', (pointer) => {
        if (pointer.leftButtonDown()) {
            tentarAtacar(this);
        }
    });

    this.cameras.main.startFollow(jogadorPrincipal);

    // Inicia os inimigos DEPOIS de criar o jogador e configurar a câmera
    simularEventosDoServidor(this);
}

function update(time, delta) {
    const velocidade = 3; 

    // Movimentação com WASD
    if (teclas.A.isDown) { 
        jogadorPrincipal.x -= velocidade;
        jogadorPrincipal.anims.play('andar-esquerda', true);
    } else if (teclas.D.isDown) { 
        jogadorPrincipal.x += velocidade;
        jogadorPrincipal.anims.play('andar-direita', true);
    } else if (teclas.W.isDown) { 
        jogadorPrincipal.y -= velocidade;
        jogadorPrincipal.anims.play('andar-cima', true);
    } else if (teclas.S.isDown) { 
        jogadorPrincipal.y += velocidade;
        jogadorPrincipal.anims.play('andar-baixo', true);
    } else {
        jogadorPrincipal.anims.stop();
    }

    // Movimentação dos outros jogadores/inimigos
    const velocidade_px_por_segundo = 100;
    for (let id in jogadores) {
        let jogador = jogadores[id];
        let dist = Phaser.Math.Distance.Between(jogador.x, jogador.y, jogador.alvoX, jogador.alvoY);

        if (dist > 2) { 
            let dx = jogador.alvoX - jogador.x;
            let dy = jogador.alvoY - jogador.y;
            let angulo = Math.atan2(dy, dx);
            jogador.x += Math.cos(angulo) * velocidade_px_por_segundo * (delta / 1000);
            jogador.y += Math.sin(angulo) * velocidade_px_por_segundo * (delta / 1000);

            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0) jogador.anims.play('andar-direita', true);
                else jogador.anims.play('andar-esquerda', true);
            } else {
                if (dy > 0) jogador.anims.play('andar-baixo', true);
                else jogador.anims.play('andar-cima', true);
            }
        } else {
            jogador.x = jogador.alvoX;
            jogador.y = jogador.alvoY;
            if (jogador.anims.isPlaying) jogador.anims.stop();
        }

        //Faz a barra de vida andar junto com o inimigo
        if (jogador.barraFundo && jogador.barraVida) {
            atualizarBarraDeVida(jogador);
        }
    }
}

//LÓGICA DE ATACAR E VIDA
function tentarAtacar(cena) {
    let inimigo = jogadores[101];
    
    if (inimigo) {
        let distancia = Phaser.Math.Distance.Between(jogadorPrincipal.x, jogadorPrincipal.y, inimigo.x, inimigo.y);
        
        if (distancia < 50) {
            let valorDano = 15;
            
            // Subtrai o dano da vida atual
            inimigo.vidaAtual -= valorDano;
            
            receberDanoVisual(cena, inimigo, valorDano);

            // Verifica se morreu
            if (inimigo.vidaAtual <= 0) {
                inimigo.vidaAtual = 0;
                matarInimigo(101);
            }
        }
    }
}

function matarInimigo(id) {
    let inimigo = jogadores[id];
    if (inimigo) {
        // Destrói as barras de vida
        inimigo.barraFundo.destroy();
        inimigo.barraVida.destroy();
        
        // Destrói o boneco do inimigo da tela
        inimigo.destroy();
        
        // Remove da lista do jogo para parar de processar ele
        delete jogadores[id];
        
        console.log("Inimigo " + id + " foi derrotado!");
    }
}

function receberDanoVisual(cena, alvo, valorDano) {
    alvo.setTint(0xff0000); 
    
    // O "if (alvo.active)" previne erro caso o inimigo tenha morrido antes do setTimeout terminar
    setTimeout(() => { if (alvo.active) alvo.clearTint(); }, 200); 

    let textoDano = cena.add.text(alvo.x - 10, alvo.y - 20, "-" + valorDano, {
        font: "bold 16px Arial", fill: "#ff0000", stroke: "#ffffff", strokeThickness: 3
    });

    cena.tweens.add({
        targets: textoDano, y: alvo.y - 50, alpha: 0, duration: 1000,
        onComplete: () => { textoDano.destroy(); }
    });
}

// DESENHANDO A BARRA DE VIDA
function atualizarBarraDeVida(personagem) {
    // Barra de vida
    let x = personagem.x - 16; // Centraliza a barra (32px / 2)
    let y = personagem.y - 25; // 25 pixels acima

    // Fundo preto (Barra vazia)
    personagem.barraFundo.clear();
    personagem.barraFundo.fillStyle(0x000000, 1);
    personagem.barraFundo.fillRect(x, y, 32, 5); // 32 de largura, 5 de altura

    // Barra Verde (Vida atual)
    personagem.barraVida.clear();
    
    // Se tiver com pouca vida (menos de 30%), a barra fica vermelha, senão verde
    let cor = (personagem.vidaAtual < personagem.vidaMax * 0.3) ? 0xff0000 : 0x00ff00;
    
    personagem.barraVida.fillStyle(cor, 1);
    
    let larguraAtual = (personagem.vidaAtual / personagem.vidaMax) * 32;
    personagem.barraVida.fillRect(x, y, larguraAtual, 5);
}

// >>> SERVIDOR (MOCK)
function simularEventosDoServidor(cena) {
    let idInimigo = 101;
    
    jogadores[idInimigo] = cena.add.sprite(100, 100, 'base_personagem');
    jogadores[idInimigo].alvoX = 100;
    jogadores[idInimigo].alvoY = 100;
    
    //Definindo a vida do inimigo
    jogadores[idInimigo].vidaMax = 50;  // Quanta vida ele tem no total
    jogadores[idInimigo].vidaAtual = 50; // Quanta vida ele tem agora
    
    // Criando os objetos gráficos que vão ser a barra de vida
    jogadores[idInimigo].barraFundo = cena.add.graphics();
    jogadores[idInimigo].barraVida = cena.add.graphics();

    setTimeout(() => { 
        if(jogadores[idInimigo]) { jogadores[idInimigo].alvoX = 300; jogadores[idInimigo].alvoY = 100; }
    }, 2000);
    setTimeout(() => { 
        if(jogadores[idInimigo]) { jogadores[idInimigo].alvoX = 250; jogadores[idInimigo].alvoY = 400; }
    }, 5000);
}