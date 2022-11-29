#include <soloud.h>
#include <soloud_wav.h>
#include <cstdio>
#include <cstdlib>
#include <cstddef>
extern "C"
{
	#include "sound.h"
	SoLoud::Soloud soloud;
	SoLoud::Wav bgm;

	SoLoud::Wav se_type;
	SoLoud::Wav se_confirm;
	SoLoud::Wav se_back;
	
	void init_sound() {
		soloud.init();
		soloud.setGlobalVolume(1.0);
		
		// load sounds
		
		size_t bgm_size = ((uintptr_t)&_binary_bgm_ogg_end - (uintptr_t)&_binary_bgm_ogg_start);
		bgm.loadMem(&_binary_bgm_ogg_start, bgm_size, false, false);
		bgm.setLooping(1);
		
		size_t se_type_size = ((uintptr_t)&_binary_se_type_wav_end - (uintptr_t)&_binary_se_type_wav_start);
		se_type.loadMem(&_binary_se_type_wav_start, se_type_size, false, false);
		
		size_t se_confirm_size = ((uintptr_t)&_binary_se_confirm_wav_end - (uintptr_t)&_binary_se_confirm_wav_start);
		se_confirm.loadMem(&_binary_se_confirm_wav_start, se_confirm_size, false, false);
		
		size_t se_back_size = ((uintptr_t)&_binary_se_back_wav_end - (uintptr_t)&_binary_se_back_wav_start);
		se_back.loadMem(&_binary_se_back_wav_start, se_back_size, false, false);
	}
	void play_bgm() {
		soloud.play(bgm);
	}
	
	void play_type_se() {
		soloud.play(se_type);
	}
	
	void play_confirm_se() {
		soloud.play(se_confirm);
	}
	
	void play_back_se() {
		soloud.play(se_back);
	}
	void finalize_sound() {
		soloud.deinit();
	}
	
}