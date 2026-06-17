savedcmd_sort.mod := printf '%s\n'   sort.o | awk '!x[$$0]++ { print("./"$$0) }' > sort.mod
