
extern unsigned char _binary_bgm_ogg_start;
extern unsigned char _binary_bgm_ogg_end;

extern unsigned char _binary_se_type_wav_start;
extern unsigned char _binary_se_type_wav_end;

extern unsigned char _binary_se_confirm_wav_start;
extern unsigned char _binary_se_confirm_wav_end;

extern unsigned char _binary_se_back_wav_start;
extern unsigned char _binary_se_back_wav_end;


void init_sound();
void finalize_sound();
void play_bgm();

void play_type_se();
void play_confirm_se();
void play_back_se();