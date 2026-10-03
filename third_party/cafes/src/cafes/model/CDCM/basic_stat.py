#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 Create basic statistics of the simulated scenarios
 Author: Alexandre Amory
"""
import os, sys, string, shutil 
import argparse
from statlib import stats

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

################################################
################################################
################################################
# GENERATE STATS FROM THE RESULTING FILES
################################################
################################################
################################################

def readScenarios(path, filename):
   tab = []
   idx = int(0)
   # get the number of subdirs in this dir. there will be erros if someone place another file in this dir
   apps = len(os.listdir(path))
   for scenario in range(int(apps)):  
      scenario = 'Scenario'+str(scenario)
      dirName = os.path.join(path,scenario)

      try:
         file = open(os.path.join(dirName,filename),'r')
      except IOError:
         #print 'ERROR: '+filename+' not found'
         continue
      line = file.readline()
      tab.append(float(line))
      file.close()

      idx = idx + 1
   # return the latency and energy lists
   return tab

def generateStats(scenarioTab):

   if len(scenarioTab) == 0:
      print 'ERROR: empty list'
      print 'Exiting application ...'
      raise
      sys.exit(1)
   try:
      avg =  stats.mean(scenarioTab)
      stddev = stats.stdev(scenarioTab)
      stddevperc = stddev*100/avg
      minTab = min(scenarioTab)
      maxTab = max(scenarioTab)
      sortedTab = sorted(scenarioTab)
      median = sortedTab[int(len(sortedTab)*0.5)]
      quartile25 = sortedTab[int(len(sortedTab)*0.25)]
      quartile75 = sortedTab[int(len(sortedTab)*0.75)]
   except ZeroDivisionError,e:
      print 'Invalid data. Div by Zero. %s' % str(e)
      print 'Exiting application ...'
      raise
      sys.exit(1)
   
   return (avg, stddev, stddevperc, minTab, maxTab, median, quartile25, quartile75)

def listToStr(list):
   # transform list to string and remove the 1st and last chars and remove white space
   statStr = str(list)
   statStr = statStr[1:len(statStr)-1]
   statStr = statStr.replace(' ','')
   return statStr

def parseArgs():
   global max_col
   global max_line
   global case_name
   global max_faults
   global enable_latency
   global enable_energy
   
   parser = argparse.ArgumentParser(description='Generate basic statistics for Fault Tolerant CAFES')
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
   # parse parameters
   parseArgs()
   
   energyTabNoFault = []
   energyTab1Fault = []
   energyTab2Fault = []
   energyTab3Fault = []
   latencyTabNoFault = []
   latencyTab1Fault = []
   latencyTab2Fault = []
   latencyTab3Fault = []

   ###############################################################
   # save the full results into a single file. no stats  included
   ###############################################################
   if enable_latency == 1:
      print 'tabulating latency data ...'
      # latency data
      fullFile = open(os.path.join(case_name,'full_latency.csv'),'w')

      # read the no fault scenarios
      latencyTabNoFault = readScenarios(os.path.join(case_name,'no_fault'), 'Latency.txt')
      fullFile.write('energy without faults:,'+listToStr(latencyTabNoFault)+'\n')
      
      if max_faults >= 1:
         # read the scenarios with single faults
         latencyTab1Fault = readScenarios(os.path.join(case_name,'with_fault','1'), 'Latency.txt')
         fullFile.write('energy without faults:,'+listToStr(latencyTab1Fault)+'\n')
      if max_faults >= 2:
         # read the scenarios with double faults
         latencyTab2Fault = readScenarios(os.path.join(case_name,'with_fault','2'), 'Latency.txt')
         fullFile.write('energy without faults:,'+listToStr(latencyTab2Fault)+'\n')
      if max_faults >= 3:
         # read the scenarios with triple faults
         latencyTab3Fault = readScenarios(os.path.join(case_name,'with_fault','3'), 'Latency.txt')
         fullFile.write('energy without faults:,'+listToStr(latencyTab3Fault)+'\n')

      fullFile.close()

   if enable_energy is True:
      print 'tabulating energy data ...'
      # write energy data
      fullFile = open(os.path.join(case_name,'full_energy.csv'),'w')

      # read the no fault scenarios
      energyTabNoFault = readScenarios(os.path.join(case_name,'no_fault'), 'Energy.txt')
      fullFile.write('energy without faults:,'+listToStr(energyTabNoFault)+'\n')

      if max_faults >= 1:
         # read the scenarios with single faults
         energyTab1Fault = readScenarios(os.path.join(case_name,'with_fault','1'), 'Energy.txt')
         fullFile.write('energy single fault:,'+listToStr(energyTab1Fault)+'\n')
      if max_faults >= 2:
         # read the scenarios with double faults
         energyTab2Fault = readScenarios(os.path.join(case_name,'with_fault','2'), 'Energy.txt')
         fullFile.write('energy double fault:,'+listToStr(energyTab2Fault)+'\n')
      if max_faults >= 3:
         # read the scenarios with triple faults
         energyTab3Fault = readScenarios(os.path.join(case_name,'with_fault','3'), 'Energy.txt')
         fullFile.write('energy triple fault:,'+listToStr(energyTab3Fault)+'\n')

      fullFile.close()      

   ################################
   # generate file with statistics
   ################################
   if enable_latency is True:
      print 'generating latency statistics...'
      summaryFile = open(os.path.join(case_name,'summary_latency.csv'),'w')

      summaryFile.write(",avg(%),avg,stddev,stddev(%),min,max,median,quartile25,quartile75\n")
      avgNoFault = generateStats(latencyTabNoFault)[0] # get only the avg
      summaryFile.write('latency without faults:,-,'+listToStr(generateStats(latencyTabNoFault))+'\n')
      if max_faults >= 1:
         avgFault = generateStats(latencyTab1Fault)[0] # get only the avg
         summaryFile.write('latency single fault:,'+str((avgFault*100/avgNoFault)-100)+','+listToStr(generateStats(latencyTab1Fault))+'\n')
      if max_faults >= 2:
         avgFault = generateStats(latencyTab2Fault)[0] # get only the avg
         summaryFile.write('latency double fault:,'+str((avgFault*100/avgNoFault)-100)+','+listToStr(generateStats(latencyTab2Fault))+'\n')
      if max_faults >= 3:
         avgFault = generateStats(latencyTab3Fault)[0] # get only the avg
         summaryFile.write('latency triple fault:,'+str((avgFault*100/avgNoFault)-100)+','+listToStr(generateStats(latencyTab3Fault))+'\n')
      summaryFile.close()

   if enable_energy is True:
      print 'generating energy statistics...'
      summaryFile = open(os.path.join(case_name,'summary_energy.csv'),'w')

      summaryFile.write(",avg(%),avg,stddev,stddev(%),min,max,median,quartile25,quartile75\n")
      avgNoFault = generateStats(energyTabNoFault)[0] # get only the avg
      summaryFile.write('energy without faults:,-,'+listToStr(generateStats(energyTabNoFault))+'\n')
      if max_faults >= 1:
         avgFault = generateStats(energyTab1Fault)[0] # get only the avg
         summaryFile.write('energy single fault:,'+str((avgFault*100/avgNoFault)-100)+','+listToStr(generateStats(energyTab1Fault))+'\n')
      if max_faults >= 2:
         avgFault = generateStats(energyTab2Fault)[0] # get only the avg
         summaryFile.write('energy double fault:,'+str((avgFault*100/avgNoFault)-100)+','+listToStr(generateStats(energyTab2Fault))+'\n')
      if max_faults >= 3:
         avgFault = generateStats(energyTab3Fault)[0] # get only the avg
         summaryFile.write('energy triple fault:,'+str((avgFault*100/avgNoFault)-100)+','+listToStr(generateStats(energyTab3Fault))+'\n')

      summaryFile.close()

   print ''
   print 'basic statistics were generated!'

# when executed, just run main():
if __name__ == '__main__':
    main()
