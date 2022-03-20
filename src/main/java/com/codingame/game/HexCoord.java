package com.codingame.game;

public class HexCoord {
	static int directions[][] = new int[][] {
		{ 0, 1,-1},
		{-1,+1, 0},
		{-1, 0,+1},
		{ 0,-1, 1},
		{ 1,-1, 0},
		{ 1, 0,-1}
	};
	int q,r,s;

	public int getQ() {
		return q;
	}

	public int getR() {
		return r;
	}

	public int getS() {
		return s;
	}

	HexCoord(int q, int r, int s) {
		this.q = q;
		this.r = r;
		this.s = s;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + q;
		result = prime * result + r;
		result = prime * result + s;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CubeCoord other = (CubeCoord) obj;
		if (q != other.q)
			return false;
		if (r != other.r)
			return false;
		if (s != other.s)
			return false;
		return true;
	}

	public HexCoord neighbour(Gravity direction) {
		int nq = q + directions[direction.getIndex()][0];
		int nr = r + directions[direction.getIndex()][1];
		int ns = s + directions[direction.getIndex()][2];
		return new HexCoord(nq, nr, ns);
	}
}
