//
//  NSString+UrlDecode.m
//  ioshello
//
//  Created by Daniel Cabrera on 25/07/14.
//
//

#import "NSString+UrlDecode.h"

@implementation NSString (UrlDecode)

- (NSString *)stringByDecodingURLFormat
{
    NSString *result = [(NSString *)self stringByReplacingOccurrencesOfString:@"+" withString:@" "];
    result = [result stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    return result;
}

@end
