import sys
import os


if __name__ == '__main__':
	instance = sys.argv[1]
	inputname = 'system/newstest2012.BLEU'
	outputname = 'avg.BLEU'
	f = open(inputname, 'r')
	res = []
	for line in f:
                line = line.replace('\n', '')
		ele = line.split(' ')
		if ele[1] == instance:
			res.append(float(ele[0]))
	f.close()
	if len(res) == 0:
		print 'Cannot find result of:', instance
	else:
		avg = sum(res)/len(res)
		w = open(outputname, 'a')
		w.write(str(avg)[:6] + ' ' + instance + '\n')
		print avg, instance
		w.close()

	
