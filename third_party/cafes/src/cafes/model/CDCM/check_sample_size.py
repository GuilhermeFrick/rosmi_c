#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 Estimate minimal sample size
 method according book:
 Bioestatistica: Princípios e Aplicacoes. Editora Artmed. Sidia M. Callegari-Jacques. 2003. Page 147
 Author: Alexandre Amory
"""
import math
import os
import argparse
import basic_stat
import candlestick  
import statistics # use function tinv to calculate the inverse of t-test
# found in http://evanjones.ca/statistics.py
# for example
#>>> statistics.tinv(0.05,29)
#2.0452309399843216

###########################################
###########################################
###########################################
# THESE ARE THE PARAMETERS THE USER MUST GIVE
###########################################
###########################################
###########################################
max_error= float() # the acceptable error
confidence_interval = float() # typically 95%, or 0.05
initial_guess = int() # initial guess for the sample size. 
# estimate the population size by using the function combin(n_routers,n_faults) in excel
# for example. COMBIN(9,2)=36
verbose = False
# scenario location
scenario = ''
enable_latency =  False  # enable latency report
enable_energy = False  # enable energy report
max_faults = 0
max_col= int() # noc size 
max_line= int() # noc size 

'''
get the Standard Error from the fault scenario
'''
def getStdDev(scenario,file):
   stdDev = 0.0

   tab = basic_stat.readScenarios(os.path.join(scenario,'with_fault',str(max_faults)), file)
   stdDev = basic_stat.generateStats(tab)[1] # get only the stddev

   return stdDev

'''
check error by router
'''
def avalErrorByRouter(sourcePath,destPath,energy):
   # create a list of candleStick class
   listRouter = []
   for line in range(max_line):
      for col in range(max_col):
         targetRouter = (line,col)
         auxData = candlestick.candleStick(sourcePath,targetRouter)
         auxData.loadData(energy,not energy)
         if len(auxData.faultLocation) <= 0:
            print 'WARNING: no data found for router [%d,%d]'% (auxData.targetRouter[0],auxData.targetRouter[1])
            continue
         listRouter.append(auxData)
         del auxData

   for candle in listRouter:
      if len(candle.faultLocation) > 0:
         if energy:
            avg, stddev, stddevperc, minTab, maxTab = basic_stat.generateStats(candle.energyList)[:5] # get only the stddev %
         else:
            avg, stddev, stddevperc, minTab, maxTab = basic_stat.generateStats(candle.latencyList)[:5] # get only the stddev %
         print 'router [%d,%d] has: stddev(%%) %.2f, stddev %.2f, sample size %d, avg %.2f, min %.2f, max %.2f, for error %.2f and confidence %.2f'% (
                                    candle.targetRouter[0],candle.targetRouter[1],stddevperc,stddev,len(candle.faultLocation),avg, minTab, maxTab, max_error,confidence_interval)
         if stddevperc > max_error or stddevperc <= 0:
            print 'WARNING: std dev %.2f not between expected threshold'% (stddevperc)
   return listRouter

'''
estimate the minimal sample size
'''
def determineSampleSize(std_dev,max_error,confidence_interval,initial_guess):
   iters = 0
   prevSampleSize = 0
   currentSampleSize = initial_guess
   global verbose
   
   while iters < 100 and prevSampleSize != currentSampleSize:
      prevSampleSize = currentSampleSize
      degreeOfFreedom = currentSampleSize -1
      if degreeOfFreedom <= 0:
         print 'WARNING: negative degreeOfFreedom'
         continue
      tTest = statistics.tinv(confidence_interval,degreeOfFreedom)
      currentSampleSize = int(math.floor(math.pow(std_dev,2) / math.pow(max_error ,2) * math.pow(tTest,2)))
      iters = iters + 1
      if verbose == True:
         print currentSampleSize

   print 'iterations:', iters
   return currentSampleSize


def parseArgs():
   global max_error
   global max_col
   global max_line
   global confidence_interval
   global initial_guess
   global verbose
   global scenario
   global max_faults
   global enable_latency
   global enable_energy

   parser = argparse.ArgumentParser(description='Estimate minimal sample size used to estimate the minimal number of simulations')
   parser.add_argument('-scenario', action='store', dest='scenario', help='The directory where the fault scenario is located.')
   parser.add_argument('-ncols', action='store', dest='max_col', type=int,help='The noc size (X axis)')
   parser.add_argument('-nlines', action='store', dest='max_line', type=int,help='The noc size (Y axis)')
   parser.add_argument('-max_error', action='store', dest='max_error', type=float,
                       help='The acceptable error between the sample mean and the population mean')
   parser.add_argument('-initial_guess', action='store', dest='initial_guess', type=int, default=100,
                       help='The initial guess for the sample size. Use the function combin(n_routers,n_faults) in excel to determine the population size')
   parser.add_argument('-confidence_interval', action='store', dest='confidence_interval', type=float,default=0.05,
                       help='The confidence interval. Default value is 0.05, i.e. 95% of confidence')
   parser.add_argument('-f', action='store', dest='max_faults', type=int,
                       help='The number of faults to be analysed')
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
   scenario      = results.scenario
   max_error = results.max_error
   confidence_interval  = results.confidence_interval
   initial_guess  = results.initial_guess
   max_faults = results.max_faults
   verbose = results.verbose
   enable_latency = results.enable_latency
   enable_energy  = results.enable_energy
   if enable_latency  == 0 and enable_energy == 0: 
      parser.error('at least enable_latency or enable_latency must be enable')
   if not os.path.isdir(scenario):
      parser.error('scenario must be a directory')
   if max_error  <= 0:
      parser.error('max_error must be > 0')
   if initial_guess  <= 0:
      parser.error('initial_guess must be > 0')
   if not(confidence_interval > 0.0 and confidence_interval < 1.0):
      parser.error('confidence_interval must be between > 0 and < 1')
   if not (max_faults  >= 1 and max_faults <= 3):
      parser.error('number_faults must be between 1 and 3')
   max_col = results.max_col
   if max_col  < 1:
      parser.error('max_col must be >= 1')
   max_line = results.max_line
   if max_line  < 1:
      parser.error('max_line must be >= 1')

   print 'scenario            =', scenario
   print 'noc_size_x          =', max_col
   print 'noc_size_y          =', max_line
   print 'max_error           =', max_error
   print 'max_faults          =', max_faults
   print 'confidence_interval =', confidence_interval
   print 'initial_guess       =', initial_guess
   print 'enable_latency      =', enable_latency
   print 'enable_energy       =', enable_energy
   print 'verbose             =', verbose
   print ''

def main():
   parseArgs()
   
   if enable_energy:
      tab = basic_stat.readScenarios(os.path.join(scenario,'with_fault',str(max_faults)), 'Energy.txt')
      avg, stddev, dev, minTab, maxTab = basic_stat.generateStats(tab)[:5] # get only the stddev %
      sample  = determineSampleSize(dev,max_error,confidence_interval,initial_guess)
      print '################'
      print 'ENERGY'
      print '################\n'
      print 'analysis of the sample of size %d ...' % len(tab)
      print 'stddev(%%) %.2f, stddev %.2f, sample size %d, avg %.2f, min %.2f, max %.2f, for error %.2f and confidence %.2f'% (dev,stddev,sample,avg, minTab, maxTab, max_error,confidence_interval)
      if dev > max_error:
         print 'WARNING: stddev %.2f > error %.2f'% (dev,max_error)
      
      print '\nanalysis by router ...'
      sourcePath = os.path.join(scenario,'with_fault',str(max_faults))
      destPath = os.path.join(scenario)
      avalErrorByRouter(sourcePath,destPath,True)

   if enable_latency:
      tab = basic_stat.readScenarios(os.path.join(scenario,'with_fault',str(max_faults)), 'Latency.txt')
      avg, stddev, dev, minTab, maxTab = basic_stat.generateStats(tab)[:5] # get only the stddev %
      sample  = determineSampleSize(dev,max_error,confidence_interval,initial_guess)
      print '################'
      print 'LATENCY'
      print '################\n'
      print 'analysis of the complete sample ...'
      print 'stddev(%%) %.2f, stddev %.2f, sample size %d, avg %.2f, min %.2f, max %.2f, for error %.2f and confidence %.2f'% (dev,stddev,sample,avg, minTab, maxTab, max_error,confidence_interval)
      if dev > max_error:
         print 'WARNING: stddev %.2f > error %.2f'% (dev,max_error)
      
      print '\nanalysis by router ...'
      sourcePath = os.path.join(scenario,'with_fault',str(max_faults))
      destPath = os.path.join(scenario)
      avalErrorByRouter(sourcePath,destPath,False)
      
   print 'done!'

# when executed, just run main():
if __name__ == '__main__':
    main()

