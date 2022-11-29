#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stddef.h>
#include <stdint.h>

char key[0xA8] = { 0x01,0x0A,0x16,0x04,0x07,0x18,0x0C,0x10,0x05,0x17,0x09,0x03,0x12,0x08,0x15,0x13,0x0B,0x02,0x0F,0x0D,0x11,0x0E,0x06,0x14,0x07,0x0C,0x0E,0x11,0x09,0x16,0x10,0x06,0x14,0x0D,0x01,0x02,0x12,0x08,0x13,0x0B,0x0F,0x0A,0x18,0x15,0x04,0x05,0x03,0x17,0x0F,0x04,0x09,0x03,0x06,0x07,0x11,0x12,0x15,0x16,0x02,0x08,0x05,0x17,0x0C,0x0D,0x01,0x18,0x0B,0x14,0x0E,0x10,0x13,0x0A,0x02,0x0A,0x0E,0x12,0x0B,0x03,0x0C,0x06,0x13,0x07,0x11,0x09,0x15,0x18,0x10,0x17,0x14,0x0F,0x04,0x01,0x05,0x08,0x16,0x0D,0x0B,0x02,0x09,0x16,0x14,0x01,0x12,0x11,0x15,0x06,0x0F,0x17,0x07,0x10,0x0C,0x0E,0x08,0x18,0x13,0x03,0x0A,0x0D,0x04,0x05,0x09,0x0F,0x05,0x0D,0x16,0x15,0x12,0x11,0x03,0x0A,0x04,0x10,0x0E,0x14,0x02,0x01,0x13,0x0C,0x06,0x0B,0x17,0x18,0x07,0x08,0x12,0x02,0x0C,0x09,0x0D,0x0E,0x04,0x07,0x16,0x14,0x17,0x01,0x11,0x03,0x10,0x15,0x08,0x0A,0x05,0x13,0x0B,0x18,0x0F,0x06 };
char* hex_values = "0123456789ABCDEF";
char* password_chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
unsigned short output_vals[6];

void substitute(char* input, char* output, int multiply_by)
{
    char* keyChar;

    keyChar = (char*)(multiply_by * 0x18 + (key));
    for(int i = 0; i < 0x18; i++) {
        output[i] = input[*keyChar + -1];
        keyChar ++;
    }
    output[0x18] = 0;
}

int the_crazy_math_part(unsigned int val1, unsigned int val2, int carry, int start_at)
{
    int c = carry;

    long long r1 = 0xFFFFFFF9;
    long long r0 = val1;
    long long r3 = val2;


    // yes this is just the asm implemented in C, dont @ me
    
    for (int i = start_at; i < 0x20; i++)
    {
        // adcs r3,r1,r3,lsl 1h
        r3 = (r1 + (r3 << 1)) + c; //same as r3 = (r3+(r1 * 2)) + c;
        c = (r3 >> 32); // set c flag
        r3 &= 0xFFFFFFFF;

        // subcc r3,r3,r1
        if (!c)
        {
            r3 = (r3 - r1);
            r3 &= 0xFFFFFFFF;
        }

        // adcs r0,r0,r0
        r0 = r0 + r0 + c;  //Same as r0 = (r0 * 2) + c;
        c = (r0 >> 32); // set c flag
        r0 &= 0xFFFFFFFF;

    }

    return r3;
}


int find_multiplier(char* system_in)
{
    int total_iterations = 0;
    int next_var = 0;
   
    char* system_in_base_ptr = system_in;
    char* hex_values_ptr = hex_values;
    for (int i = 0; i < strlen(system_in_base_ptr); i++)
    {
        char c = *system_in++;
        char characters[2];
        memset(characters, 0, 2);
        characters[0] = c;

        char* a2b = strstr(hex_values_ptr, characters);
        next_var = (uintptr_t)a2b - (uintptr_t)hex_values_ptr;
        if (!a2b)
            next_var = 0;
        total_iterations += next_var;
    }
    // step 2

    unsigned int offset = 7;

    if (offset <= total_iterations)
    {
        int c = 0;
        int start_at = 0x1c;
        unsigned int r3 = total_iterations >> 4;
        if (offset <= r3 >> 0xC)
        {
            start_at -= 0x10;
            r3 >>= 0x10;
        }
        if (offset <= r3 >> 0x10)
        {
            start_at -= 0x8;
            r3 >>= 0x8;
        }
        if (offset <= r3)
        {
            start_at -= 0x4;
            r3 >>= 0x4;
        }

        unsigned int r0 = total_iterations << (start_at & 0xFF);


        c = ((long long int)r0 * 2) > 0xFFFFFFFF;
        r0 = r0 * 2;

        printf("Starting Carry: %x\n", c);


        return the_crazy_math_part(r0, r3, c, start_at);
    }
    return 0;
}

unsigned int hex_to_bytes(char* input, int iterator)
{
    char* iteration;
    char* final_char;
    int result;
    int i;
    char* current_char;
    char* enc = hex_values;
    result = 0;
    i = 0;
    current_char = input + iterator;
    int ii = 0xc;
    do {
        char curChar[2];
        memset(curChar, 0x00, 2);
        curChar[0] = current_char[0];

        iteration = strstr(enc, curChar);
        final_char = iteration + -(intptr_t)enc;
        if (iteration == (char*)0x0) {
            final_char = (char*)0x0;
        }
        i = i + 1;
        result = result + ((uintptr_t)final_char << (ii & 0xff));
        ii = (ii - 4) & 0xffff;
        current_char = current_char + 1;
    } while (i < 4);
    return result & 0xffff;
}

void generate_password(unsigned char* inbuf, char* output)
{
    int i;
    i = 0;
    unsigned short* input = (unsigned short*) inbuf;
    do {
        output[i] = password_chars[input[i] - 1];
        i = i + 1;
    } while (i < 6);
    output[i] = '\0';
    return;
}

int decode(char* encoded, char* outbuf){
    int iterator = 0;
    int i = 0;
    int ii = 0;

    memset(outbuf, 0x00, 12);
    unsigned short* decoded = (unsigned short*) outbuf;
    do {
        int chr = hex_to_bytes(encoded, iterator);
        i = ii + 1;
        decoded[ii] = (unsigned short)chr;
        iterator = iterator + 4;
        ii = i;
    } while (i < 6);

    i = 0; 
    ii = 0;
    do
    {
        i = ii;
        int chr = decoded[ii++];
        decoded[i] = chr % 33 + 1;
    } while (ii < 6);
    
    return 0;
}


void ecdp_keygen(char* mac_address, char* maccas_id, char* mannager_id, char* output_key, size_t output_size) {
    char formatted[190];
    char encoded[64];
    char final_key[100];
    char decoded[12];
    
    snprintf(formatted, sizeof(formatted), "%s%s%s", mac_address, maccas_id, mannager_id);
    int multiplier = find_multiplier(formatted);
    substitute(formatted, encoded, multiplier);
    decode(encoded, decoded); 
    generate_password((unsigned char*)decoded, final_key);
	
	strncpy(output_key, final_key, output_size);
}
