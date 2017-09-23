package de.andre.data.impl;

public class MathUtil {

    /**
     * @param dividend
     * @param divisor
     * @return lowest int that is greater or equal to dividend / divisor.
     */
    public static int ceilDiv(int dividend, int divisor) {
	if (dividend % divisor == 0) {
	    return (dividend / divisor);
	}
	return (dividend / divisor) + 1;
    }

    /**
     * @param dividend
     * @param divisor
     * @return highest int that is lower or equal to dividend / divisor.
     */
    public static int floorDiv(int dividend, int divisor) {
	return dividend / divisor;
    }
    
    /**
     * @param dividend
     * @param divisor
     * @return dividend modulo divisor or dividend % divisor
     */
    public static int mod(int dividend, int divisor) {
	return dividend % divisor;
    }

    /**
     * @param value
     *            the original field value
     * @param position
     *            bit positions to shift
     * @param mask
     *            bitwise Mask. always a bit value like 1 or 111
     * @return the bits at the given position. Examples:
     *         <ul>
     *         <li><code>extractBits('101<b>01</b>1101', 4, '11')</code> is 01
     *         </li>
     *         <li><code>extractBits('101011<b>101</b>', 0, '111')</code> is 101
     *         </li>
     *         <li><code>extractBits('10101<b>110</b>1', 1, '111')</code> is 110
     *         </li>
     *         </ul>
     */
    public static int extractBits(int value, int position, int mask) {
	return (value >> position) & mask;
    }

    /**
     * Manipulates the given value by setting the partValue at the given
     * position
     * 
     * @param value
     *            the original field value
     * @param position
     *            bit positions to shift
     * @param mask
     *            bitwise Mask. always a bit value like 1 or 111
     * @param partValue
     *            the value to set
     * @return the new value. Examples:
     *         <ul>
     *         <li><code>setBits('101<b>01</b>1101', 4, '11', '00')</code> is
     *         101<b>00</b>1101</li>
     *         <li><code>setBits('101011<b>101</b>', 0, '111', '010')</code> is
     *         101011<b>010</b></li>
     *         <li><code>setBits('10101<b>110</b>1', 1, '111', '010')</code> is
     *         10101<b>010</b>1</li>
     *         </ul>
     */
    public static int setBits(int value, int position, int mask, int partValue) {
	int cleared = value & ~(mask << position);
	return cleared | (partValue << position);
    }
}
