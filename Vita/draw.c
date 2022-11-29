#include "draw.h"
#include <vita2d.h>
#include <string.h>

const char* MAC_DEVIDER = ":";

vita2d_texture *bg;
vita2d_texture *logo;

vita2d_texture *box_unsel;
vita2d_texture *box_sel;

vita2d_pgf *pgf;
vita2d_pvf *pvf; 

void start_draw(){
	vita2d_start_drawing();
	vita2d_clear_screen();
	
	vita2d_draw_texture(bg, 0, 0);
	vita2d_draw_texture(logo, (SCREEN_WIDTH/2) - (LOGO_WIDTH/2), 45);		
}

void end_draw(){
	vita2d_end_drawing();
	vita2d_swap_buffers();	
}

void finalize_draw(){
	vita2d_fini();
	vita2d_free_texture(bg);
	vita2d_free_texture(logo);
	vita2d_free_texture(box_unsel);
	vita2d_free_texture(box_sel);
	vita2d_free_pgf(pgf);
	vita2d_free_pvf(pvf);
}
void init_draw(){
	vita2d_init();
	vita2d_set_clear_color(RGBA8(0xFF, 0xFF, 0xFF, 0xFF));
	
	pgf = vita2d_load_default_pgf();
	pvf = vita2d_load_default_pvf();

	bg = vita2d_load_PNG_buffer(&_binary_bg_png_start);
	logo = vita2d_load_PNG_buffer(&_binary_logo_png_start);

	box_unsel = vita2d_load_PNG_buffer(&_binary_box_unsel_png_start);
	box_sel = vita2d_load_PNG_buffer(&_binary_box_sel_png_start);

}
void draw_text_center(int y, char* msg) {
	float size = 1.0f;
	int textWidth = 0;
	int textHeight = 0;
	vita2d_pgf_text_dimensions(pgf, size, msg, &textWidth, &textHeight);
	int textPos = (SCREEN_WIDTH/2) - (textWidth/2);
	vita2d_pgf_draw_text(pgf, textPos, y, COLOR_WHITE, size, msg);
}

void draw_mac_picker(int y, char* octlets, int pos) {
	float size = 1.0f;

	size_t totalOctlets = strlen(octlets);
	char octlet[2];
	memset(octlet, 0, sizeof(octlet));
	
	int deviderWidth = 0;
	int deviderHeight = 0;	
	vita2d_pgf_text_dimensions(pgf, size, MAC_DEVIDER, &deviderWidth, &deviderHeight);
	
	int boxX = ((SCREEN_WIDTH/2) - (totalOctlets * (BOX_WIDTH + BOX_SPACING))/2);
	
	for(int i = 0; i < totalOctlets; i++) {
		octlet[0] = octlets[i];
		int octletWidth = 0;
		int octletHeight = 0;	
		
		vita2d_pgf_text_dimensions(pgf, size, octlet, &octletWidth, &octletHeight);
		int octletPosX = boxX +((BOX_WIDTH/2) - (octletWidth/2));
		int octletPosY = y + ((BOX_HEIGHT - octletHeight) + (BOX_HEIGHT/2 - octletHeight/2));
		
		if( i == pos ) {
			vita2d_draw_texture(box_sel, boxX, y);	
			vita2d_pgf_draw_text(pgf, octletPosX, octletPosY, SELECTED_COLOR, size, octlet);
		}
		else {
			vita2d_draw_texture(box_unsel, boxX, y);
			vita2d_pgf_draw_text(pgf, octletPosX, octletPosY, UNSELECTED_COLOR, size, octlet);
		}
		
		
		if( ((i+1) % 2) == 0 && (i+1 < totalOctlets)) {
			boxX += (BOX_WIDTH + (BOX_SPACING/2));
			int deviderPosY = y + ((BOX_HEIGHT - deviderHeight) + (BOX_HEIGHT/2 - deviderHeight/2));
			vita2d_pgf_draw_text(pgf, boxX, deviderPosY, COLOR_WHITE, size, MAC_DEVIDER);
			boxX += deviderWidth + (BOX_SPACING/2);
		}
		else{
			boxX += (BOX_WIDTH + BOX_SPACING);
		}
			
	}
}

void draw_number_picker(int y, char* digits, int pos) {
	float size = 1.0f;

	size_t totalDigits = strlen(digits);


	char digit[2];
	memset(digit, 0, sizeof(digit));

	
	int boxX = (SCREEN_WIDTH/2) - (totalDigits * (BOX_WIDTH + BOX_SPACING))/2;
	
	for(int i = 0; i < totalDigits; i++) {
		digit[0] = digits[i];
		int digitWidth = 0;
		int digitHeight = 0;	
		
		vita2d_pgf_text_dimensions(pgf, size, digit, &digitWidth, &digitHeight);
		int digitPosX = boxX +((BOX_WIDTH/2) - (digitWidth/2));
		int digitPosY = y + ((BOX_HEIGHT - digitHeight) + (BOX_HEIGHT/2 - digitHeight/2));
		
		if( i == pos ) {
			vita2d_draw_texture(box_sel, boxX, y);	
			vita2d_pgf_draw_text(pgf, digitPosX, digitPosY, SELECTED_COLOR, size, digit);
		}
		else {
			vita2d_draw_texture(box_unsel, boxX, y);
			vita2d_pgf_draw_text(pgf, digitPosX, digitPosY, UNSELECTED_COLOR, size, digit);
		}
		
		boxX += (BOX_WIDTH + BOX_SPACING);
	}
}