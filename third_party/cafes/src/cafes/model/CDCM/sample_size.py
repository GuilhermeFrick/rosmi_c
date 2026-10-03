#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 Estimate minimal sample size.
 The script can can get the std dev in two modes: get it from an existing scenario, or get it from parameter
 
 method according book:
 Bioestatistica: Princípios e Aplicacoes. Editora Artmed. Sidia M. Callegari-Jacques. 2003. Page 147
 Author: Alexandre Amory
"""
import math
import os
import argparse
import basic_stat 
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
std_dev = 0.0 # If this is diff than 0.0, then it uses this std dev to calculate the sample size. Otherwise
# it gest the std dev from a fault scenario

'''
get the Standard Error from the fault scenario
'''
def getStdDev(scenario,file):
   stdDev = 0.0

   tab = basic_stat.readScenarios(os.path.join(scenario,'with_fault',str(max_faults)), file)
   stdDev = basic_stat.generateStats(tab)[1] # get only the stddev

   return stdDev

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
      tTest = statistics.tinv(confidence_interval,degreeOfFreedom)
      currentSampleSize = int(math.floor(math.pow(std_dev,2) / math.pow(max_error ,2) * math.pow(tTest,2)))
      iters = iters + 1
      if verbose == True:
         print currentSampleSize

   print 'iterations:', iters
   return currentSampleSize


def parseArgs():
   global max_error
   global confidence_interval
   global initial_guess
   global verbose
   global scenario
   global std_dev
   global max_faults
   global enable_latency
   global enable_energy

   parser = argparse.ArgumentParser(description='Estimate minimal sample size used to estimate the minimal number of simulations')
   parser.add_argument('-scenario', action='store', dest='scenario',  default='', help='The directory where the fault scenario is located.')
   parser.add_argument('-max_error', action='store', dest='max_error', type=float,
                       help='The acceptable error between the sample mean and the population mean')
   parser.add_argument('-stddev', action='store', dest='std_dev', type=float,  default=0.0, help='The assume a given std dev')
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
   std_dev = results.std_dev
   if std_dev < 0.0:
      parser.error('std_dev must be positive')
   if enable_latency  and enable_energy:
      parser.error('at least enable_latency or enable_latency must be enable')
   # check if it is in scenario mode
   if std_dev == 0.0:
      if len(scenario) == 0:
         parser.error('either stddev or scenario must be defined')
      elif  not os.path.isdir(scenario):
         parser.error('scenario must be a directory')
   # check if it is in stddev mode
   else:
      if len(scenario) > 0:
         parser.error('either stddev or scenario must be defined')
   if max_error  <= 0:
      parser.error('max_error must be > 0')
   if initial_guess  <= 0:
      parser.error('initial_guess must be > 0')
   if not(confidence_interval > 0.0 and confidence_interval < 1.0):
      parser.error('confidence_interval must be between > 0 and < 1')
   if not (max_faults  >= 1 and max_faults <= 3):
      parser.error('number_faults must be between 1 and 3')

   print 'scenario            =', scenario
   print 'max_error           =', max_error
   print 'max_faults          =', max_faults
   print 'std_dev             =', std_dev
   print 'confidence_interval =', confidence_interval
   print 'initial_guess       =', initial_guess
   print 'enable_latency      =', enable_latency
   print 'enable_energy       =', enable_energy
   print 'verbose             =', verbose
   print ''

def main():
   parseArgs()
   
   if enable_energy:
      if std_dev == 0.0:
         tab = basic_stat.readScenarios(os.path.join(scenario,'with_fault',str(max_faults)), 'Energy.txt')
         dev = basic_stat.generateStats(tab)[2] # get only the stddev %
      else:
         dev = std_dev
      sample  = determineSampleSize(dev,max_error,confidence_interval,initial_guess)
      print 'ENERGY'
      print 'stddev %.2f, sample size %d for error %.2f and confidence %.2f'% (dev,sample,max_error,confidence_interval)
   if enable_latency:
      if stddev == 0.0:
         tab = basic_stat.readScenarios(os.path.join(scenario,'with_fault',str(max_faults)), 'Latency.txt')
         dev = basic_stat.generateStats(tab)[2] # get only the stddev %
      else:
         dev = stddev
      sample  = determineSampleSize(dev,max_error,confidence_interval,initial_guess)
      print 'LATENCY'
      print 'stddev %.2f, sample size %d for error %.2f and confidence %.2f'% (dev,sample,max_error,confidence_interval)
   
   print 'done!'

# when executed, just run main():
if __name__ == '__main__':
    main()
