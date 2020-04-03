package com.teamgannon.trips.routeplanning;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * assume this is square
 */
@Slf4j
@Data
public class TriangularMatrix {

    /**
     * the actual backing store for hte matrix
     */
    private double[] flattenedMatrix;

    /**
     * number of dimensions since this is a square matrix
     */
    private int numDims;

    public TriangularMatrix(int numDims) {
        this.numDims = numDims;
        int size = (numDims - 1) * numDims / 2;
        this.flattenedMatrix = new double[size];
        log.debug("created triangular flattenedMatrix with {} elements", size);
    }


    public void set(int i, int j, double value) {
        int position = getPosition(i, j);
        if (position >= 0) {
            flattenedMatrix[position] = value;
        }
    }


    public double get(int i, int j) {
        int position = getPosition(i, j);
        if (position < 0) {
            return 0;
        }
        return flattenedMatrix[position];
    }

    private int getPosition(int i, int j) {
        if (i >= numDims || j >= numDims) {
            return -2;
        }
        // main diagonal is alway 0
        if (i == j) {
            return -1;
        }
        // prior to possible swap (if needed)
        int ii = i;
        int jj = j;

        // swap dimensions if we find ourselves on lower triangel since it is symmetric to upper
        if (i < j) {
            ii = j;
            jj = i;
        }
        // calculate position in array
        return getI(ii, jj) + getJ(jj);
    }

    private int getJ(int j) {
        int value = 0;
        if (j != 0) {
            for (int ind = 1; ind <= j; ind++) {
                value += (numDims - ind);
            }
        }
        return value;
    }

    /**
     * get the mapping in the triangular flattenedMatrix mapping for i,j based on i
     *
     * @param i the i position (columnar)
     * @param j the j position (row)
     * @return the i index offset in arrary representation
     */
    private int getI(int i, int j) {
        //check to flip
        if (i < j) {
            return j - i - 1;
        } else {
            return i - j - 1;
        }
    }

    public static void main(String[] args) {
        TriangularMatrix matrix = new TriangularMatrix(5);
        int i1 = matrix.getI(0, 4);
        int i2 = matrix.getI(0, 3);
        int i3 = matrix.getI(1, 2);
        int i4 = matrix.getI(1, 4);
        int i5 = matrix.getI(3, 4);
        int i6 = matrix.getI(2, 4);


        int j1 = matrix.getJ(0);
        int j2 = matrix.getJ(1);
        int j3 = matrix.getJ(2);
        int j4 = matrix.getJ(3);
        int j5 = matrix.getJ(4);
        int jbig = matrix.getJ(6);

        int ij1 = matrix.getPosition(4, 3);
        int ijalt = matrix.getPosition(3, 4);

        int ij2 = matrix.getPosition(0, 3);
        int ij2alt = matrix.getPosition(3,0);

        int ij3 = matrix.getPosition(1,2);
        int ij3alt = matrix.getPosition(2,1);

        int ij4 = matrix.getPosition(4, 1);
        int ij4alt = matrix.getPosition(1,4);

        int ij5 = matrix.getPosition(4, 3);
        int ij5alt = matrix.getPosition(3,4);

        int ij6 = matrix.getPosition(4, 2);
        int ij6alt = matrix.getPosition(2,4);

        log.info("test complete");
    }

}

















