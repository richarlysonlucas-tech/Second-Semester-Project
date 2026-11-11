
class AudioManager {
    
constructor() {
    this.sounds = {};
    this.music = null;
    this.masterVolume = 0.7;
    this.sfxVolume = 0.8;
    this.musicVolume = 0.5;

    // Carregar os novos efeitos sonoros
    this.loadSound("zombie", "assets/audio/zombie/zombie.mp3");
    this.loadSound("golem", "assets/audio/golem/golem.mp3");
    this.loadSound("magic", "assets/audio/magic/magic.mp3");
    this.loadSound("regeneration", "assets/audio/regeneration/regeneration.mp3");
    this.loadSound("killEnemy", "assets/audio/kill enemy/kill enemy.mp3");
    this.loadSound("gameOver", "assets/audio/game over/game over.mp3");
   
this.loadSound("sword", "assets/audio/combat/combat.mp3");

this.loadSound("walking", "assets/audio/footsteps/footsteps.mp3");

this.loadSound("collect", "assets/audio/rewards/rewards.mp3");

this.loadSound("interface", "assets/audio/ui/ui.mp3");
}




    loadSound(name, path) {
        const audio = new Audio(path);
        audio.preload = "auto";
        this.sounds[name] = audio;
    }

    playSound(name) {
        const audio = this.sounds[name];

        if (!audio) {
            console.warn("Áudio não encontrado:", name);
            return;
        }

        const sound = audio.cloneNode();
        sound.volume = this.masterVolume * this.sfxVolume;

        sound.play().catch(error => {
            console.warn("Erro no áudio:", error);
        });
    }

    playMusic(path) {
        this.stopMusic();

        this.music = new Audio(path);
        this.music.loop = true;
        this.music.volume =
            this.masterVolume * this.musicVolume;

        this.music.play().catch(error => {
            console.warn("Erro na música:", error);
        });
    }

    stopMusic() {
        if (this.music) {
            this.music.pause();
            this.music.currentTime = 0;
            this.music = null;
        }
    }

    setMasterVolume(volume) {
        this.masterVolume = Math.max(0, Math.min(1, volume));

        if (this.music) {
            this.music.volume =
                this.masterVolume * this.musicVolume;
        }
    }

    setSfxVolume(volume) {
        this.sfxVolume = Math.max(0, Math.min(1, volume));
    }

    setMusicVolume(volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));

        if (this.music) {
            this.music.volume =
                this.masterVolume * this.musicVolume;
        }
    }
}

export default AudioManager;
