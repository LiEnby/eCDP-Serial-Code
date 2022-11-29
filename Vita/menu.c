#include "menu.h"
#include "draw.h"
#include "eCDP.h"
#include "sound.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <vitasdk.h>

static int pos = 0;
static char StoreNo[64];
static char SerialNo[64];
static char MacAddress[64];
static char OutputCode[64];
static EcdpKeygenState state;

void randomize_octlets(char* octlets, size_t numbOctlets){	
	for(int i = 0; i < numbOctlets; i++){
		int rng = (rand() % 0xF);
		
		if(rng <= 9)
			octlets[i] = ('0' + rng);
		else
			octlets[i] = ('A' + rng-9);
	}
	octlets[numbOctlets+1] = 0;
	
}

void clear_data(char* digits, size_t numbDigits){
	memset(digits, '0', numbDigits);
}

void randomize_digits(char* digits, size_t numbDigits){	
	for(int i = 0; i < numbDigits; i++)
		digits[i] = ('0' + (rand() % 10));
	digits[numbDigits+1] = 0;
	
}

void wait_for_release(SceCtrlData oldPad){
	SceCtrlData curPad;
	memset(&curPad, 0, sizeof(curPad));
	sceCtrlPeekBufferPositive(0, &curPad, 1);
	do {
		sceCtrlPeekBufferPositive(0, &curPad, 1);
		
	} while(curPad.buttons == oldPad.buttons);
	
}
SceCtrlData get_buttons() {
	SceCtrlData pad;
	memset(&pad, 0, sizeof(pad));
	sceCtrlPeekBufferPositive(0, &pad, 1);
	if(pad.buttons != 0)
		wait_for_release(pad);
	return pad;
}

void handle_mac_picker(char* octlets, EcdpKeygenState nextState){
	SceCtrlData pad = get_buttons();
	
	size_t totalOctlets = strlen(octlets);
	
	if(pos < 0) pos = 0;
	if(pos >= totalOctlets) pos = totalOctlets-1;
	
	char octlet = octlets[pos];
	
	switch(pad.buttons){
		case SCE_CTRL_UP:
			play_type_se();
			octlet++;
			break;
		case SCE_CTRL_DOWN:
			play_type_se();
			octlet--;
			break;
		case SCE_CTRL_LEFT:
			play_type_se();
			pos--;
			return;
		case SCE_CTRL_RIGHT:
			play_type_se();
			pos++;
			return;
		case SCE_CTRL_TRIANGLE:
			play_confirm_se();
			randomize_octlets(octlets, totalOctlets);
			return;
		case SCE_CTRL_CIRCLE:
			play_back_se();
			clear_data(octlets, totalOctlets);
			return;
		case SCE_CTRL_CROSS:
			play_confirm_se();
			state = nextState;
			pos = 0;
			return;		
	}
	
	if(octlet == '9' + 1) octlet = 'A';
	if(octlet == 'A' - 1) octlet = '9';
	if(octlet > 'F') octlet = '0';
	if(octlet < '0') octlet = 'F';
	
	octlets[pos] = octlet;
	
}

void handle_number_picker(char* digits, EcdpKeygenState nextState){
	SceCtrlData pad = get_buttons();
	
	size_t totalDigits = strlen(digits);
	
	if(pos < 0) pos = 0;
	if(pos >= totalDigits) pos = totalDigits-1;
	
	char digit = digits[pos];
	
	
	switch(pad.buttons){
		case SCE_CTRL_UP:
			play_type_se();
			digit++;
			break;
		case SCE_CTRL_DOWN:
			play_type_se();
			digit--;
			break;
		case SCE_CTRL_LEFT:
			play_type_se();
			pos--;
			return;
		case SCE_CTRL_RIGHT:
			play_type_se();
			pos++;
			return;
		case SCE_CTRL_TRIANGLE:
			play_confirm_se();
			randomize_digits(digits, totalDigits);
			return;
		case SCE_CTRL_CIRCLE:
			play_back_se();
			clear_data(digits, totalDigits);
			return;
		case SCE_CTRL_CROSS:
			play_confirm_se();
			state = nextState;
			pos = 0;
			return;		
	}
	
	
	if(digit > '9') digit = '0';
	if(digit < '0') digit = '9';
	
	digits[pos] = digit;
	
}

void init_menus(){
	pos = 0;
	memset(StoreNo, 0, sizeof(StoreNo));
	memset(SerialNo, 0, sizeof(SerialNo));
	memset(MacAddress, 0, sizeof(MacAddress));
	memset(OutputCode, 0, sizeof(OutputCode));
	
	state = INPUT_MGR_NO;
	
	clear_data(StoreNo, 6);
	clear_data(SerialNo, 6);
	clear_data(MacAddress, 12);
}

void process_enter_mgr_no(){
	handle_number_picker(StoreNo, INPUT_SERIAL_NO);
}

void process_enter_serial_no(){
	handle_number_picker(SerialNo, INPUT_MAC_ADDRESS);
}

void process_enter_mac_address(){
	handle_mac_picker(MacAddress, OUTPUT_CODE);
	
	if(state == OUTPUT_CODE) {
		ecdp_keygen(MacAddress, StoreNo, SerialNo, OutputCode, sizeof(OutputCode));
	}
}

void process_output_code(){
	SceCtrlData pad = get_buttons();
	
	switch(pad.buttons){
		case SCE_CTRL_CROSS:
			play_confirm_se();
			init_menus();
			break;
		case SCE_CTRL_CIRCLE:
			play_back_se();
			sceKernelDelayThread(260000);
			sceKernelExitProcess(0);
			break;
	}
}

void draw_end(){
	draw_text_center(500, "× Generate Another / ○ Close Application");	
}

void draw_controls(){
	draw_text_center(500, "↑↓← → Change Input / △ Randomize Input / ○ Clear Input / × Confirm Input");	
}

void draw_enter_mgr_no(){
	draw_text_center(260, "Enter Store Number:");
	draw_number_picker(280, StoreNo, pos);
	draw_controls();
}

void draw_enter_serial_no(){
	draw_text_center(260, "Enter Serial Number of DS Card:");
	draw_number_picker(280, SerialNo, pos);
	draw_controls();
}

void draw_enter_mac_addr(){
	draw_text_center(260, "Enter DS Mac Address:");
	draw_mac_picker(280, MacAddress, pos);
	draw_controls();
}

void draw_output_code(){
	draw_text_center(260, "Donald McDonald speaks with a cryptic message:");
	draw_number_picker(280, OutputCode, -1);
	draw_end();
}

void show_menu(){
	switch(state){
		case INPUT_MGR_NO:
			draw_enter_mgr_no();
			break;
		case INPUT_SERIAL_NO:
			draw_enter_serial_no();
			break;
		case INPUT_MAC_ADDRESS:
			draw_enter_mac_addr();
			break;
		case OUTPUT_CODE:
			draw_output_code();
			break;
	}
}

void process_menu() {
	switch(state){
		case INPUT_MGR_NO:
			process_enter_mgr_no();
			break;
		case INPUT_SERIAL_NO:
			process_enter_serial_no();
			break;
		case INPUT_MAC_ADDRESS:
			process_enter_mac_address();
			break;
		case OUTPUT_CODE:
			process_output_code();
			break;
	}
}