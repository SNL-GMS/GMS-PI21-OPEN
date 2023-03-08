#ifndef GMS_FILTER_CONSTANTS_H
#define GMS_FILTER_CONSTANTS_H

#define MAX_NAME_SIZE 64
#define MAX_COMMENT_SIZE 256

// Maximum filter order and coefficients supported
#define MAX_FILTER_ORDER 20
#define MAX_POLES MAX_FILTER_ORDER
#define MAX_SOS MAX_POLES * 3
#define MAX_TRANSFER_FUNCTION 1024

// Up to 10 filters in a filter cascade
#define MAX_FILTER_DESCRIPTIONS 10

#endif // GMS_FILTER_CONSTANTS_H