#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 Create gnuplot's candlestick charts for the simulated scenarios
 Author: Alexandre Amory
"""
import os, sys, math
import argparse
import basic_stat

###########################################
###########################################
###########################################
# THESE ARE THE PARAMETERS THE USER MUST GIVE
###########################################
###########################################
###########################################
# change here the size of the NoC
max_col= int() # noc size 
max_line= int() # noc size 
case_name = '' # this is the name of the dir
max_faults = int() # only 1 to 3 faults are allowed
enable_latency =  False  # enable latency report
enable_energy = False  # enable energy report

###########################################
###########################################
###########################################
# CANDLESTICK CHART
###########################################
###########################################
###########################################

class candleStick:
   # list of set of tuples used to store the fault locations. ex: [set((1,0),(0,2)),set((1,1),(1,2))]
   faultLocation = []
   # list of energy values of float type
   energyList = []
   # list of latency values of int type
   latencyList = []
   path = '' # path to the dir with scenarios
   targetRouter = () # tuple with target router
   
   def __init__(self, dirPath, router):
     self.path = dirPath
     self.targetRouter = router
     self.faultLocation = []
     self.energyList = []
     self.latencyList = []

   def getFaultPosition(self,line):
      # get the part 'R[line, col]'
      auxStr = line.split(':')[0]
      # eliminate 'R[' and ']' chars
      auxStr = auxStr[2:len(auxStr)-1]
      # eliminate whitespace
      auxStr = auxStr.replace(' ','')
      auxList = auxStr.split(',')
      # return line and col
      return (int(auxList[0]),int(auxList[1]))

   def printTuple(self,XYTuple):
      return '['+str(XYTuple[0])+','+str(XYTuple[1])+']'

   def printData(self):
      tableStr = ''
      if len(self.faultLocation) == 0:
         return ''
      else:
         if enable_energy and (len(self.faultLocation) != len(self.energyList)):
            print 'ERROR: the number of itens must be the same'
            print len(self.faultLocation), len(self.energyList)
            return ''
         if enable_latency and (len(self.faultLocation) != len(self.latencyList)):
            print 'ERROR: the number of itens must be the same'
            print len(self.faultLocation), len(self.latencyList)
            return ''

         numberFaults = len(self.faultLocation[0])
         # print the table header
         for j in range(numberFaults):
            tableStr = tableStr + '"fault'+str(j+1)+'",'
         if enable_energy is True:
            tableStr = tableStr + '"energy",'
         if enable_latency is True:
            tableStr = tableStr + '"latency"'
         tableStr = tableStr + '\n'
         # print the table content
         for i in range(len(self.faultLocation)):
            # print the fault locations
            for j in range(numberFaults):
               tableStr = tableStr +'\"'+self.printTuple(self.faultLocation[i][j])+ '\",'
            # print energy and latency
            if enable_energy is True:
               tableStr = tableStr +str(self.energyList[i])+','
            if enable_latency is True:
               tableStr = tableStr +str(self.latencyList[i])
            tableStr = tableStr + '\n'
         return tableStr

   def loadData(self,enable_energy,enable_latency):
      scenarios= len(os.listdir(self.path))
      for scenario in range(scenarios):  
         scenario = 'Scenario'+str(scenario)
         dirName = os.path.join(self.path,scenario)
         
         # get the fault location
         try:
            file = open(os.path.join(dirName,'faults.txt'),'r')
         except IOError:
            print 'ERROR: faults.txt not found'
            continue
         auxList = [] # temporaly saves the list of faults
         auxXY = []   # temp tuple
         found = '0'
         # tests whether this scenario has a fault in the targetRouter
         for line in file:
            # ignores the lines with spare position
            if 'X' not in line:
               auxXY = self.getFaultPosition(str(line))
               auxList.append((auxXY[0],auxXY[1]))
               if self.targetRouter == (auxXY[0],auxXY[1]):
                  found = '1'
                  #print 'found:', self.targetRouter, auxXY
         file.close()
         
         # this  scenario affects the targetRouter
         if found == '1':
            if enable_energy is True:
               # get the energy
               try:
                  file = open(os.path.join(dirName,'Energy.txt'),'r')
               except IOError:
                  #print 'ERROR: Energy.txt not found'
                  continue
               line = file.readline()
               self.energyList.append(float(line))
               file.close()
            if enable_latency is True:
               # get the latency
               try:
                  file = open(os.path.join(dirName,'Latency.txt'),'r')
               except IOError:
                  print 'ERROR: Latency.txt not found'
                  continue
               line = file.readline()
               self.latencyList.append(int(line))
               file.close()
            # include it only if the energy or latency files are found
            self.faultLocation.append(auxList)


def generateCandlePlotFile(sufix,min_value, max_value,destPath):
   print '   writing gnuplot file ...'
   gnuplotFile = open(os.path.join(destPath,'candlestick_'+sufix+'.plot'),'w')
   if sufix == 'energy':
      gnuplotFile.write('set title "energy consumption overhead when tile is faulty"\n')
      gnuplotFile.write('set ylabel "uJ(%)"\n')
   else:
      gnuplotFile.write('set title "latency overhead when tile is faulty"\n')
      gnuplotFile.write('set ylabel "clock cycles(%)"\n')
   gnuplotFile.write('set xlabel "faulty tile"\n')
   gnuplotFile.write('set boxwidth 0.2 absolute\n')
   gnuplotFile.write('set xrange [ 0 : '+str(max_col*max_line+1)+' ] noreverse nowriteback\n')
   # zoom the chart between the minimal - 2% and the maximal + 2% value
   gnuplotFile.write('set yrange [ '+str(min_value-(min_value*0.05))+' : '+str(max_value*1.05)+' ] noreverse nowriteback\n')
   # it should generate a line like this: set xtics   ("[0,0]" 1, "[0,1]" 2, "[0,2]" 3, "[1,0]" 4, "[1,1]" 5, "[1,2]" 6, "[2,0]" 7, "[2,1]" 8, "[2,2]" 9)
   gnuplotFile.write('set xtics   (')
   idx = 1
   auxStr = ''
   for line in range(max_line):
      for col in range(max_col):
         auxStr =  auxStr + '"['+str(line)+','+str(col)+']" '+str(idx)+', ' 
         idx = idx + 1
   # remove the last ',' and insert a ')'
   auxStr = auxStr[:len(auxStr)-2]+')\n'
   gnuplotFile.write(auxStr)
   gnuplotFile.write('plot \'candlestick_'+sufix+'.dat\' using 1:3:2:6:5 with candlesticks notitle whiskerbars 0.5 , \'\' using 1:4:4:4:4 with candlesticks lt rgb "red" notitle, \'\' using 1:7 with lines lt rgb "red" notitle\n')
   gnuplotFile.write('set terminal postscript eps enhance\n')
   gnuplotFile.write('set output \'candlestick_'+sufix+'.eps\'\n')
   gnuplotFile.write('replot\n')
   gnuplotFile.close()   
   
def generateCandleDatFile(sufix,listRouter,destPath):
   ###################################
   # generate gnuplot .dat file with the following format:
   # X Min 1stQuartile Median 3rdQuartile Max Avg
   ###################################
   print '   writing dat file ...'
   datFile = open(os.path.join(destPath,'candlestick_'+sufix+'.dat'),'w')
   idx = 1
   max_value = 0
   min_value = sys.maxint
   
   # get fault-free average
   if sufix == 'energy':
      FaultFreeAvg = basic_stat.generateStats(basic_stat.readScenarios(os.path.join(case_name,'no_fault'), 'Energy.txt'))[0]
   else:
      FaultFreeAvg = basic_stat.generateStats(basic_stat.readScenarios(os.path.join(case_name,'no_fault'), 'Latency.txt'))[0]
   
   for candle in listRouter:
      if len(candle.faultLocation) > 0:
         if sufix == 'energy':
            (avg, stddev, stddevperc, minTab, maxTab, median, quartile25, quartile75) = basic_stat.generateStats(candle.energyList)
         else:
            (avg, stddev, stddevperc, minTab, maxTab, median, quartile25, quartile75) = basic_stat.generateStats(candle.latencyList)
         if max_value < maxTab:
            max_value = maxTab
         if min_value > minTab:
            min_value = minTab
         datFile.write(str(idx)+' '+str((minTab*100/FaultFreeAvg)-100)+' '+str((quartile25*100/FaultFreeAvg)-100)+' '+str((median*100/FaultFreeAvg)-100)+' '+str((quartile75*100/FaultFreeAvg)-100)+' '+str((maxTab*100/FaultFreeAvg)-100)+' '+str((avg*100/FaultFreeAvg)-100)+'\n')
      idx = idx + 1
   datFile.close()
   return math.floor(min_value*100/FaultFreeAvg)-100, math.ceil(max_value*100/FaultFreeAvg)-100
   
def generateCandleStickChart(sourcePath,destPath):
   # create a list of candleStick class
   listRouter = []
   for line in range(max_line):
      for col in range(max_col):
         targetRouter = (line,col)
         auxData = candleStick(sourcePath,targetRouter)
         auxData.loadData(enable_energy,enable_latency)
         csvFile = open(os.path.join(destPath,'candlestick'+str(line)+'_'+str(col)+'.csv'),'w')
         csvFile.write(auxData.printData())
         csvFile.close()
         listRouter.append(auxData)
         del auxData
      
   # generate stats required by the .dat file
   if enable_energy is True:
      print 'generating energy chart...'
      min_value, max_value = generateCandleDatFile('energy',listRouter,destPath)
      generateCandlePlotFile('energy',min_value, max_value,destPath)
   if enable_latency is True:
      print 'generating latency chart...'
      min_value, max_value = generateCandleDatFile('latency',listRouter,destPath)
      generateCandlePlotFile('latency',min_value, max_value,destPath)

def parseArgs():
   global max_col
   global max_line
   global case_name
   global max_faults
   global enable_latency
   global enable_energy
   
   parser = argparse.ArgumentParser(description='Generate candle stick chart for Fault Tolerant CAFES')
   parser.add_argument('-ncols', action='store', dest='max_col', type=int,
                       help='The noc size (X axis)')
   parser.add_argument('-nlines', action='store', dest='max_line', type=int,
                       help='The noc size (Y axis)')
   parser.add_argument('-c', action='store', dest='case_name',
                       help='The name of the dir with simulation results')
   parser.add_argument('-f', action='store', dest='max_faults', type=int,
                       help='The number of faults to be analysed')
   parser.add_argument('-l', action='store_true', default=False,
                       dest='enable_latency',
                       help='Enable analysis of latency')
   parser.add_argument('-e', action='store_true', default=False,
                       dest='enable_energy',
                       help='Enable analysis of energy')
   parser.add_argument('-v', action='version', version='%(prog)s 1.0')
   results = parser.parse_args()

   # more error checking
   case_name      = results.case_name
   enable_latency = results.enable_latency
   enable_energy  = results.enable_energy
   if enable_latency  == 0 and enable_energy == 0: 
      parser.error('at least enable_latency or enable_latency must be enable')
   max_col = results.max_col
   if max_col  < 1:
      parser.error('max_col must be >= 1')
   max_line = results.max_line
   if max_line  < 1:
      parser.error('max_line must be >= 1')
   max_faults = results.max_faults
   if not (max_faults  >= 1 and max_faults <= 3):
      parser.error('number_faults must be between 1 and 3')

   print 'noc_size_x     =', max_col
   print 'noc_size_y     =', max_line
   print 'case_name      =', case_name
   print 'number_faults  =', max_faults
   print 'enable_latency =', enable_latency
   print 'enable_energy  =', enable_energy
   print ''

def main():
   parseArgs()
   sourcePath = os.path.join(case_name,'with_fault',str(max_faults))
   destPath = os.path.join(case_name)
   generateCandleStickChart(sourcePath,destPath)
   os.chdir(case_name)
   if enable_energy:
      os.system('gnuplot < candlestick_energy.plot')
   if enable_latency:
      os.system('gnuplot < candlestick_latency.plot')

   print ''
   print 'candlestick chart has been generated!'

# when executed, just run main():
if __name__ == '__main__':
    main()
