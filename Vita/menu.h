typedef enum {
	INPUT_MGR_NO,
	INPUT_SERIAL_NO,
	INPUT_MAC_ADDRESS,
	OUTPUT_CODE
} EcdpKeygenState;

void init_menus();
void show_menu();
void process_menu();