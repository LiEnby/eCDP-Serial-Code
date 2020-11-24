#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <Windows.h>
BYTE key[0xA8] = { 0x01,0x0A,0x16,0x04,0x07,0x18,0x0C,0x10,0x05,0x17,0x09,0x03,0x12,0x08,0x15,0x13,0x0B,0x02,0x0F,0x0D,0x11,0x0E,0x06,0x14,0x07,0x0C,0x0E,0x11,0x09,0x16,0x10,0x06,0x14,0x0D,0x01,0x02,0x12,0x08,0x13,0x0B,0x0F,0x0A,0x18,0x15,0x04,0x05,0x03,0x17,0x0F,0x04,0x09,0x03,0x06,0x07,0x11,0x12,0x15,0x16,0x02,0x08,0x05,0x17,0x0C,0x0D,0x01,0x18,0x0B,0x14,0x0E,0x10,0x13,0x0A,0x02,0x0A,0x0E,0x12,0x0B,0x03,0x0C,0x06,0x13,0x07,0x11,0x09,0x15,0x18,0x10,0x17,0x14,0x0F,0x04,0x01,0x05,0x08,0x16,0x0D,0x0B,0x02,0x09,0x16,0x14,0x01,0x12,0x11,0x15,0x06,0x0F,0x17,0x07,0x10,0x0C,0x0E,0x08,0x18,0x13,0x03,0x0A,0x0D,0x04,0x05,0x09,0x0F,0x05,0x0D,0x16,0x15,0x12,0x11,0x03,0x0A,0x04,0x10,0x0E,0x14,0x02,0x01,0x13,0x0C,0x06,0x0B,0x17,0x18,0x07,0x08,0x12,0x02,0x0C,0x09,0x0D,0x0E,0x04,0x07,0x16,0x14,0x17,0x01,0x11,0x03,0x10,0x15,0x08,0x0A,0x05,0x13,0x0B,0x18,0x0F,0x06 };
char* hex_values = "0123456789ABCDEF";
char* password_chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
unsigned short output_vals[6];

void substitute(char* input, char* output, int multiply_by)
{
    int i;
    char* keyChar;

    i = 0;
    keyChar = (char*)(multiply_by * 0x18 + (key));
    for(int i = 0; i < 0x18; i++) {
        output[i] = input[*keyChar + -1];
        keyChar ++;
    }
    output[0x18] = 0;
}

int the_crazy_math_part(unsigned int val1, unsigned int val2)
{
    int c = 0;
    
    long long r1 = 0xFFFFFFF9;
    long long r0 = val1;
    long long r3 = val2;


    // yes this is just the asm implemented in C, dont @ me
    
    for (int i = 0; i < 4; i++)
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


char* ascii_to_byte(char* enc, char* input)

{
    int i;
    int ii;
    char* iii;
    char c;

    c = *enc;
    i = 0;
    while (1) {
        if (c == '\0') {
            return (char*)0x0;
        }
        ii = 0;
        iii = enc + i;
        while (c = input[ii], c != '\0' && (*iii == c)) {
            iii = iii + 1;
            ii = ii + 1;
        }
        if (c == '\0') break;
        i = i + 1;
        c = enc[i];
    }
    return enc + i;
}


int find_multiplier(char* system_in, unsigned int maccasId)
{
    int system_in_len;
    byte* next_var;
    byte* this_char;
    byte* extraout_r1;
    unsigned int total_iterations;
    int i = 0;
    int ii = 0;
    byte* system_in_2;
    unsigned int c;
    char* starting_inc;

    total_iterations = 0;
    c = maccasId & 0xffff00ff;
    system_in_len = strlen(system_in);
    starting_inc = hex_values;
    system_in_2 = (byte*)system_in;
    if (0 < system_in_len) {
        do {
            system_in_2 = system_in_2 + 1;
            c = c & 0xffffff00 | (unsigned int)*system_in_2;
            next_var = ascii_to_byte((byte*)starting_inc, (byte*)&c);
            this_char = next_var + -(int)starting_inc;
            if (next_var == (byte*)0x0) {
                this_char = (byte*)0x0;
            }
            i = i + 1;
            total_iterations = (unsigned int)(this_char + total_iterations);
            system_in_len = strlen(system_in);
            system_in_2 = system_in_2;
        } while (i < system_in_len);
    }

    // step 2

    int ret;
    unsigned int offset = 7;

    if (offset <= total_iterations) {
        i = 0x1c;
        ii = total_iterations >> 4;
        if ((int)offset <= (int)(total_iterations >> 0x10)) {
            i = 0xc;
            ii = total_iterations >> 0x14;
        }
        if ((int)offset <= (int)(ii >> 4)) {
            i = i - 8;
            ii = ii >> 8;
        }
        if ((int)offset <= (int)ii) {
            i = i - 4;
            ii = ii >> 4;
        }

        return the_crazy_math_part((total_iterations << (i & 0xff)) * 2, ii);
    }
    return 0;
}

unsigned int hex_to_bytes(char* input, int iterator, int multiplier)
{
    byte* iteration;
    byte* final_char;
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

        iteration = ascii_to_byte(enc, curChar);
        final_char = iteration + -(int)enc;
        if (iteration == (char*)0x0) {
            final_char = (char*)0x0;
        }
        i = i + 1;
        result = result + ((int)final_char << (ii & 0xff));
        ii = ii - 4 & 0xffff;
        current_char = current_char + 1;
    } while (i < 4);
    return result & 0xffff;
}

void generate_password(unsigned short* input, char* output)
{
    int i;
    i = 0;
    do {
        output[i] = password_chars[input[i] - 1];
        i = i + 1;
    } while (i < 6);
    output[i] = '\0';
    return;
}

int main()
{
    char maccas_id[7];
    char mannager_id[7];
    char mac_address[18];
    char formatted[64];
    char encoded[64];
    char temp_key[100];
    char final_key[100];
    char total_output [64];
    printf("eCDP Serial Number Generator (By SilicaAndPina)\n");
    printf("-- A backdoor on the worlds rarest DS game.\n");
    printf("Enter your NDS's Mac Address (without any seperators): ");
    gets_s(mac_address, 18);
    printf("Enter McDonalds Store Id (first 6 digit entry): ");
    gets_s(maccas_id,7);
    printf("Enter McDonalds Manager Id (second 6 digit entry): ");
    gets_s(mannager_id, 7);


    snprintf(formatted, 64, "%s%s%s", mac_address, maccas_id, mannager_id);
    printf("Formatted Data: %s\n", formatted);

    int multiplier = find_multiplier(formatted, (unsigned int)maccas_id);
    printf("Multiplier: %x\n", multiplier);
    substitute(formatted, encoded, multiplier);
    printf("Encoded Data: %s\n", encoded);

    unsigned short password_values[6];
    memset(password_values, 0x00, 6 * 2);
    int iterator = 0;
    int i = 0;
    int ii = 0;
    do {
        int chr = hex_to_bytes(encoded, iterator, multiplier);
        i = ii + 1;
        password_values[ii] = (unsigned short)chr;
        iterator = iterator + 4;
        ii = i;
    } while (i < 6);
    
    printf("Password Values 1: ");
    for (int i = 0; i < 6; i++)
    {
        printf("%x ", password_values[i]);
    }
    printf("\n");
    int magic_number = 0x3E0F83E1;

    ii = 0;
    do {
        short chr = password_values[ii];
        iterator = ii + 1;
        password_values[ii] = chr + (short)(int)((long long)magic_number * (long long)(int)(unsigned int)chr >> 0x23) * -0x21 + 1;
        ii = iterator;
    } while (iterator < 6);
    
    printf("Password Values 2: ");
    for (int i = 0; i < 6; i++)
    {
        printf("%x ", password_values[i]);
    }
    printf("\n");
    generate_password(password_values, final_key);
    printf("Ronald McDonald Says your password is %s\n", final_key);

    printf("\n\nThou hast been reversed!");
    while (1) {};
}
