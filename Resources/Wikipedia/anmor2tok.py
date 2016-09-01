import sys

def next_token(line):
  mylist = []
  i = 0
  while i < len(line):
    if line[i] == "\\":
      mylist.append(line[i])
      mylist.append(line[i+1])
      i += 2
    elif line[i] == "^":
      if len(mylist) > 0:
        yield "".join(mylist)
      mylist = [line[i]]
      i += 1
    elif line[i] == "$":
      mylist.append(line[i])
      yield "".join(mylist)
      mylist = []
      i += 1
    else:
      mylist.append(line[i])
      i += 1
      
  if len(mylist) > 0:
    yield "".join(mylist)

  return

for i in sys.stdin:
  result = []
  for j in next_token(i):
    if j[0:1] == "^" and j[-1:] == "$":
      result.append(j[1:].split("/")[0])
    else:
      result.append(j)
  sys.stdout.write(" ".join(" ".join(result).split()).strip("[] "))
  sys.stdout.write("\n")