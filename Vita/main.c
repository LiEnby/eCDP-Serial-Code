/*
 * Written by Li (2022/28/NOV)
 * Trans Lefts.
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <vitasdk.h>
#include <time.h>

#include "sound.h"
#include "draw.h"
#include "menu.h"


int main() {
	time_t t;
	srand((unsigned int) time(&t));
	init_sound();
	init_draw();
	init_menus();
	
	play_bgm();
	while (1) {
		start_draw();
		show_menu();
		end_draw();
		
		process_menu();
	}

	finalize_draw();
	finalize_sound();
	sceKernelExitProcess(0);
	return 0;
}
