#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 Create gnuplot's heat charts for the simulated scenarios
 Author: Alexandre Amory
"""
import os, sys, math
import argparse
import basic_stat 
from candlestick import candleStick

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
# HEATMAP CHART
###########################################
###########################################
###########################################

def generateHeatPlotFile(sufix,min_value, max_value,destPath):
   print '   writing gnuplot file ...'
   gnuplotFile = open(os.path.join(destPath,'heat_'+sufix+'.plot'),'w')
   
   if sufix == 'energy':
      gnuplotFile.write('set title "energy consumption overhead when the tile is faulty"\n')
      gnuplotFile.write('set cblabel "uJ(%) compared to the fault-free system"\n')
   else:
      gnuplotFile.write('set title "latency overhead when the tile is faulty"\n')
      gnuplotFile.write('set cblabel "clock cycles(%) compared to the fault-free system"\n')

   gnuplotFile.write('unset key\n')

   gnuplotFile.write('set palette gray negative\n')
   gnuplotFile.write('# The default coloer scheme is gray scale.\n')
   gnuplotFile.write('# however, if you prefer color, just uncomment\n')
   gnuplotFile.write('# the lines below\n')
   gnuplotFile.write('#set palette rgbformula -7,2,-7\n')
   gnuplotFile.write('#set palette rgbformula 33,13,10\n')
   gnuplotFile.write('set cbrange ['+str(min_value-(min_value*0.05))+' : '+str(max_value*1.05)+']\n')
   gnuplotFile.write('set xlabel "columns"\n')
   gnuplotFile.write('set ylabel "lines"\n')
   gnuplotFile.write('set ytics 1\n')
   gnuplotFile.write('set xtics 1\n')

   gnuplotFile.write('set xrange [-0.5:'+str(max_col-1)+'.5]\n')
   gnuplotFile.write('set yrange [-0.5:'+str(max_line-1)+'.5]\n')

   gnuplotFile.write('set view map\n')
   gnuplotFile.write('set terminal postscript eps enhance\n')
   gnuplotFile.write('set output \'heat_'+sufix+'.eps\'\n')
   gnuplotFile.write('splot \'heat_'+sufix+'.dat\' matrix with image\n')
   gnuplotFile.close()   
   
def generateHeatDatFile(sufix,sourcePath,destPath):
   ###################################
   # generate gnuplot .dat file with the following format:
   # Avg0_0 Avg0_1 Avg0_2
   # Avg1_0 Avg1_1 Avg1_2
   # Avg2_0 Avg2_1 Avg2_2
   ###################################
   print '   writing dat file ...'
   max_value = 0
   min_value = sys.maxint
   datFile = open(os.path.join(destPath,'heat_'+sufix+'.dat'),'w')
   
   # get fault-free average
   if sufix == 'energy':
      FaultFreeAvg = basic_stat.generateStats(basic_stat.readScenarios(os.path.join(case_name,'no_fault'), 'Energy.txt'))[0]
   else:
      FaultFreeAvg = basic_stat.generateStats(basic_stat.readScenarios(os.path.join(case_name,'no_fault'), 'Latency.txt'))[0]

   for line in range(max_line):
      for col in range(max_col):
         # load data
         targetRouter = (line,col)
         auxData = candleStick(sourcePath,targetRouter)
         auxData.loadData(enable_energy,enable_latency)
         if len(auxData.faultLocation) > 0:
            if sufix == 'energy':
               (avg, stddev, stddevperc, minTab, maxTab, median, quartile25, quartile75) = basic_stat.generateStats(auxData.energyList)
            else:
               (avg, stddev, stddevperc, minTab, maxTab, median, quartile25, quartile75) = basic_stat.generateStats(auxData.latencyList)
            if max_value < maxTab:
               max_value = maxTab
            if min_value > minTab:
               min_value = minTab
            datFile.write(str((avg*100/FaultFreeAvg)-100)+' ')
            #print 'XY:',targetRouter, avg
            del auxData
      datFile.write('\n')

   datFile.close()
   return math.floor(min_value*100/FaultFreeAvg)-100, math.ceil(max_value*100/FaultFreeAvg)-100
   
def generateHeatChart(sourcePath,destPath):
   if enable_energy == 1:
      print 'generating energy chart...'
      min_value, max_value = generateHeatDatFile('energy',sourcePath,destPath)
      generateHeatPlotFile('energy',min_value, max_value,destPath)
   if enable_latency== 1:
      print 'generating latency chart...'
      min_value, max_value = generateHeatDatFile('latency',sourcePath,destPath)
      generateHeatPlotFile('latency',min_value, max_value,destPath)

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
      parser.error('noc_size_x must be >= 1')
   max_line = results.max_line
   if max_line  < 1:
      parser.error('noc_size_y must be >= 1')
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
   generateHeatChart(sourcePath,destPath)
   os.chdir(case_name)
   if enable_energy:
      os.system('gnuplot < heat_energy.plot')
   if enable_latency:
      os.system('gnuplot < heat_latency.plot')

   print ''
   print 'heat chart has been generated!'

# when executed, just run main():
if __name__ == '__main__':
    main()
