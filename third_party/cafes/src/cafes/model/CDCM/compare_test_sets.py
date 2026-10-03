#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 compare teo entire test sets. Usually an exaustive test set and a sample.
 This is used to check if the sample error is within acceptable boundaries.
 Author: Alexandre Amory
"""
import math
import os
import argparse
import basic_stat
import candlestick

###########################################
###########################################
###########################################
# THESE ARE THE PARAMETERS THE USER MUST GIVE
###########################################
###########################################
###########################################
max_error= float() # the acceptable error
# reference scenario location
exhaustive_scenario = ''
# location of the scenario to be evaluated 
sample_scenario = ''
enable_latency =  False  # enable latency report
enable_energy = False  # enable energy report
max_col= int() # noc size 
max_line= int() # noc size 
verbose = False

def parseArgs():
   global exhaustive_scenario
   global sample_scenario
   global max_error
   global max_col
   global max_line
   global verbose
   global enable_latency
   global enable_energy

   parser = argparse.ArgumentParser(description='Estimate minimal sample size used to estimate the minimal number of simulations')
   parser.add_argument('-exhaustive_scenario', action='store', dest='exhaustive_scenario', 
      help='The directory where the exhaustive scenario is located. This is the reference scenario.')
   parser.add_argument('-sample_scenario', action='store', dest='sample_scenario', 
      help='The directory where the fault sample scenario is located. This scenario is comrared against the exhaustive')
   parser.add_argument('-ncols', action='store', dest='max_col', type=int,help='The noc size (X axis)')
   parser.add_argument('-nlines', action='store', dest='max_line', type=int,help='The noc size (Y axis)')
   parser.add_argument('-max_error', action='store', dest='max_error', type=float,
                       help='The acceptable error between the sample mean and the population mean')
   parser.add_argument('-l', action='store_true', default=False,
                       dest='enable_latency',
                       help='Enable analysis of latency')
   parser.add_argument('-e', action='store_true', default=False,
                       dest='enable_energy',
                       help='Enable analysis of energy')
   parser.add_argument('-verbose', action='store_true', default=False,dest='verbose',
                       help='Enable verbose')
   parser.add_argument('-v', action='version', version='%(prog)s 1.0')
   results = parser.parse_args()

   # more error checking
   exhaustive_scenario  = results.exhaustive_scenario
   sample_scenario      = results.sample_scenario
   max_error            = results.max_error
   verbose              = results.verbose
   enable_latency       = results.enable_latency
   enable_energy        = results.enable_energy
   if enable_latency  == 0 and enable_energy == 0: 
      parser.error('at least enable_latency or enable_latency must be enable')
   if not os.path.isdir(exhaustive_scenario):
      parser.error('exhaustive_scenario must be a directory')
   if not os.path.isdir(sample_scenario):
      parser.error('sample_scenario must be a directory')
   if max_error  <= 0:
      parser.error('max_error must be > 0')
   max_col = results.max_col
   if max_col  < 1:
      parser.error('max_col must be >= 1')
   max_line = results.max_line
   if max_line  < 1:
      parser.error('max_line must be >= 1')

   print 'exhaustive_scenario =', exhaustive_scenario
   print 'sample_scenario     =', sample_scenario
   print 'noc_size_x          =', max_col
   print 'noc_size_y          =', max_line
   print 'max_error           =', max_error
   print 'enable_latency      =', enable_latency
   print 'enable_energy       =', enable_energy
   print 'verbose             =', verbose
   print ''

def main():
   parseArgs()
   
   if enable_energy:
      print '################'
      print 'ENERGY'
      print '################\n'
      print 'analysis of the fault-free sample ...'
      reftab = basic_stat.readScenarios(os.path.join(exhaustive_scenario,'no_fault'), 'Energy.txt')
      evaltab = basic_stat.readScenarios(os.path.join(sample_scenario,'no_fault'), 'Energy.txt')
      refavg, refstddev, refdev, refminTab, refmaxTab = basic_stat.generateStats(reftab)[:5]
      evalavg, evalstddev, evaldev, evalminTab, evalmaxTab = basic_stat.generateStats(evaltab)[:5]
      error = math.fabs(((evalavg*100.0)/refavg) - 100.0)
      print 'Exaustive: avg %d, stddev(%%) %.2f, stddev %.2f, min %d, max %d'% (refavg, refdev, refstddev,  refminTab, refmaxTab)
      print 'Sample: avg %d, stddev(%%) %.2f, stddev %.2f, min %d, max %d'% (evalavg, evaldev, evalstddev,  evalminTab, evalmaxTab)
      print 'diff between avg ref %d and avg sample %d is %.2f' % (refavg,evalavg,error)
      if error > max_error:
         print 'WARNING: the difference betwwen the averages is more than the acceptable difference. Exaustive avg %d, Sample avg %d, error %.2f, max_error %.2f' \
            %  (refavg, evalavg, error, max_error)
      del reftab
      del evaltab

      nFaults = len(os.listdir(os.path.join(exhaustive_scenario,'with_fault')))
      for fault in range(nFaults):
         print 'analysis of the sample with %d faults ...'% (fault+1)
         reftab = basic_stat.readScenarios(os.path.join(exhaustive_scenario,'with_fault',str(fault+1)), 'Energy.txt')
         evaltab = basic_stat.readScenarios(os.path.join(sample_scenario,'with_fault',str(fault+1)), 'Energy.txt')
         if len(reftab) == 0:
            print 'WARNING: empty ref dir. skipping analysis with %d faults'% (fault+1)
            continue
         if len(evaltab) == 0:
            print 'WARNING: empty sample dir. skipping analysis with %d faults'% (fault+1)
            continue
         refavg, refstddev, refdev, refminTab, refmaxTab = basic_stat.generateStats(reftab)[:5]
         evalavg, evalstddev, evaldev, evalminTab, evalmaxTab = basic_stat.generateStats(evaltab)[:5]
         error = math.fabs(((evalavg*100.0)/refavg) - 100.0)
         print 'Exaustive: avg %d, stddev(%%) %.2f, stddev %.2f, min %d, max %d'% (refavg, refdev, refstddev, refminTab, refmaxTab)
         print 'Sample: avg %d, stddev(%%) %.2f, stddev %.2f, min %d, max %d'% (evalavg, evaldev, evalstddev, evalminTab, evalmaxTab)
         print 'diff between avg ref %d and avg sample %d is %.2f' % (refavg,evalavg,error)
         if error > max_error:
            print 'WARNING: the difference betwwen the averages is more than the acceptable difference. Exaustive avg %d, Sample avg %d, error %.2f, max_error %.2f' \
               % (refavg, evalavg, error, max_error)

         print 'analysis per router ...'
         reflistRouter = []
         evallistRouter = []
         for line in range(max_line):
            for col in range(max_col):
               targetRouter = (line,col)
               # load ref data
               refauxData = candlestick.candleStick(os.path.join(exhaustive_scenario,'with_fault',str(fault+1)),targetRouter)
               refauxData.loadData(enable_energy,not enable_energy)
               reflistRouter.append(refauxData)
               del refauxData
               # load sample data
               evalauxData = candlestick.candleStick(os.path.join(sample_scenario,'with_fault',str(fault+1)),targetRouter)
               evalauxData.loadData(enable_energy,not enable_energy)
               evallistRouter.append(evalauxData)
               del evalauxData

         i = 0
         while i < len(reflistRouter) and i < len(evallistRouter):
            refData = reflistRouter[i]
            evalData = evallistRouter[i]
            if (len(refData.faultLocation) <= 0) or (len(evalData.faultLocation) <= 0):
               print 'ERROR: no data for router [%d,%d]' % (refData.targetRouter[0],refData.targetRouter[1])
               continue
            if refData.targetRouter != evalData.targetRouter:
               print 'ERROR: the router idx are diff. ref is [%d,%d] and sample is [%d,%d]' % \
                  (refData.targetRouter[0],refData.targetRouter[1],evalData.targetRouter[0],evalData.targetRouter[1])
               continue
            refavg, refstddev, refstddevperc, refminTab, refmaxTab = basic_stat.generateStats(refData.energyList)[:5]
            evalavg, evalstddev, evalstddevperc, evalminTab, evalmaxTab = basic_stat.generateStats(evalData.energyList)[:5]
            error = math.fabs(((evalavg*100.0)/refavg) - 100.0)
            print 'Exaustive router [%d,%d]: avg %d, stddev(%%) %.2f, stddev %.2f, min %d, max %d'% \
               (refData.targetRouter[0],refData.targetRouter[1],refavg, refstddevperc, refstddev, refminTab, refmaxTab)
            print 'Sample router [%d,%d]: avg %d, stddev(%%) %.2f, stddev %.2f, min %d, max %d'% \
               (evalData.targetRouter[0],evalData.targetRouter[1],evalavg, evalstddevperc, evalstddev, evalminTab, evalmaxTab)
            print 'diff between avg ref %d and avg sample %d is %.2f' % (refavg,evalavg,error)
            if error > max_error:
               print 'WARNING: the difference betwwen the averages is more than the acceptable difference. Exaustive avg %d, \
                  Sample avg %d, error %.2f, max_error %.2f' % (refavg, evalavg, error, max_error)
            i = i + 1

   if enable_latency:
      print 'not implemented'
      
   print 'done!'

# when executed, just run main():
if __name__ == '__main__':
    main()


