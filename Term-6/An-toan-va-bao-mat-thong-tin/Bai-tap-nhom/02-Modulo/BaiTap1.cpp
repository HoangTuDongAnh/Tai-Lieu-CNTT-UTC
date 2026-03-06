#include <iostream>
using namespace std;

long long modHBLT(long long a, long long m, long long n) {
	long long res = 1;
	a = a % n;
	
	while (m > 0) {
		if (m % 2 == 1) {
			res = (res * a) % n;
		}
		
		a = (a * a) % n;
		m = m / 2;
	}
	
	return res;
}

int main() {
	long long a = 499, m = 6337, n = 6337;
	
	long long b = modHBLT(a, m, n);
	
	cout << "Ket qua: " << b << endl;
	
	return 0;
}
