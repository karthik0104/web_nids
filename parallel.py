import multiprocessing as mp
import paramiko

class ParallelGA():
  """Class which performs rule creation parallely"""
  def __init__(self):
    self.output = mp.Queue()
    self.ssh = [[None]]*10
    self.stdin = [[None]]*10
    self.stdout = [[None]]*10
    self.stderr = [[None]]*10

  # define a example function
  def ssh_remote_command(self,host_ip_address,pos,command):
    """ Executes a process on remote machine """
    self.ssh[pos] = paramiko.SSHClient()
    self.ssh[pos].set_missing_host_key_policy(paramiko.AutoAddPolicy())
    self.ssh[pos].connect(host_ip_address,username='karthik',password='1234')
    self.stdin[pos],self.stdout[pos],self.stderr[pos] = self.ssh[pos].exec_command(command)
    self.output.put((pos,self.stdout[pos].readlines()))
    self.stdin[pos].close()
    self.ssh[pos].close()

  def run_parallel(self):	
    # Setup a list of processes that we want to run
    processes = [mp.Process(target=self.ssh_remote_command, args=('192.168.1.103',1,"python test.py")),mp.Process(target=self.ssh_remote_command, args=('192.168.1.103',2,"python test.py"))]
    # Run processes
    for p in processes:
        p.start()
    # Exit the completed processes
    for p in processes:
        p.join()
    # Get process results from the output queue
    results = [self.output.get() for p in processes]
    print(results)

x = ParallelGA()
x.run_parallel()
