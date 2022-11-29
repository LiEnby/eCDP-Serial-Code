
#define SCREEN_WIDTH 960
#define SCREEN_HEIGHT 544

#define LOGO_WIDTH 424
#define LOGO_HEIGHT 160

#define BOX_HEIGHT 32
#define BOX_WIDTH 32
#define BOX_SPACING 8

#define COLOR_WHITE ( RGBA8(251,251,251,255) )
#define COLOR_BLACK ( RGBA8(0,0,0,255) )

#define UNSELECTED_COLOR COLOR_WHITE
#define SELECTED_COLOR COLOR_BLACK

extern unsigned char _binary_bg_png_start;
extern unsigned char _binary_logo_png_start;

extern unsigned char _binary_box_unsel_png_start;
extern unsigned char _binary_box_sel_png_start;


void init_draw();
void finalize_draw();

void start_draw();
void end_draw();

void draw_text_center(int y, char* msg);
void draw_number_picker(int y, char* digits, int pos);
void draw_mac_picker(int y, char* octlets, int pos);

