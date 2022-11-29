TITLE_ID = ECDP00001
TARGET   = eCDPKeyGen
OBJS     = eCDP.o sound.o main.o menu.o draw.o bg.o logo.o box_unsel.o box_sel.o bgm.o se_back.o se_confirm.o se_type.o

LIBS = -lvita2d -lSceDisplay_stub -lSceGxm_stub \
	-lSceSysmodule_stub -lSceCtrl_stub -lScePgf_stub -lScePvf_stub \
	-lSceCommonDialog_stub -lfreetype -lpng -ljpeg -lz -lc -lSceAppMgr_stub \
	-lsoloud -lpthread -lSceAudio_stub -lstdc++ -lm

PREFIX  = arm-vita-eabi
CC      = $(PREFIX)-gcc
CXX		= $(PREFIX)-g++
CFLAGS  = -Wl,-q -Wall -fno-lto
CXXFLAGS  = -Wl,-q -Wall -std=c++11
ASFLAGS = $(CFLAGS)

all: $(TARGET).vpk

%.vpk: eboot.bin
	vita-mksfoex -s TITLE_ID=$(TITLE_ID) "$(TARGET)" param.sfo
	vita-pack-vpk -s param.sfo -b eboot.bin -a sce_sys/icon0.png=sce_sys/icon0.png -a sce_sys/pic0.png=sce_sys/pic0.png -a sce_sys/livearea/contents/bg.png=sce_sys/livearea/contents/bg.png -a sce_sys/livearea/contents/template.xml=sce_sys/livearea/contents/template.xml $@

eboot.bin: $(TARGET).velf
	vita-make-fself -s $< $@

%.velf: %.elf
	vita-elf-create $< $@

$(TARGET).elf: $(OBJS)
	$(CC) $(CFLAGS) $^ $(LIBS) -o $@
%.o: %.cpp
	$(CXX) -c $(CXXFLAGS) $^ $(LIBS) -o $@
	
%.o: %.png
	$(PREFIX)-ld -r -b binary -o $@ $^

%.o: %.ogg
	$(PREFIX)-ld -r -b binary -o $@ $^

%.o: %.wav
	$(PREFIX)-ld -r -b binary -o $@ $^
clean:
	@rm -rf $(TARGET).vpk $(TARGET).velf $(TARGET).elf $(OBJS) \
		eboot.bin param.sfo
