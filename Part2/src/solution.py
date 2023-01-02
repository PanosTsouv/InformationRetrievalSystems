import numpy as np
import numpy.linalg


for i in range(100):
    print('Start reading termXdocArray.txt file....')

    file1 = open('termXdocArray.txt', 'r')
    allDocList = []
    for line in file1:
        docArrayAsString = np.array(line.split(' '))
        docArrayAsInt = np.asarray(docArrayAsString, dtype = float)
        docArrayAsInt = docArrayAsInt.reshape( (1, -1) )
        allDocList.append(docArrayAsInt)
    file1.close()

    print('Close termXdocArray.txt file. Reading was successfully')

    print('Start reading termXqueries.txt file....')

    file2 = open('termXqueries.txt', 'r')
    allQueriesList = []
    for line in file2:
        queryArrayAsString = np.array(line.split(' '))
        queryArrayAsInt = np.asarray(queryArrayAsString, dtype = float)
        queryArrayAsInt = queryArrayAsInt.reshape( (1, -1) )
        allQueriesList.append(queryArrayAsInt)
    file2.close()

    print('Close termXqueries.txt file. Reading was successfully')

    print()

    print('Merge all term X queries arrays to 2d array terms X queries....')
    q = numpy.concatenate( allQueriesList, axis=0 )
    print('Shape of terms X queries array is')
    print(q.shape)

    print('Merge all term X docs arrays to 2d array terms X docs....')
    A = numpy.concatenate( allDocList, axis=0 )
    print('Shape of terms X docs array is')
    print(A.shape)

    print()

    print('SVD analysis starting...')
    U, S, V = numpy.linalg.svd(A)
    print('SVD analysis done...')

    S = np.diag(S)
    print('U shape is:')
    print(U.shape)
    print('S shape is:')
    print(S.shape)
    print('V shape is:')
    print(V.shape)

    print()

    rList = [50,150,300]
    for r in rList:
        print('--------------------------------------- r = {} ---------------------------------------'.format(r))
        print('Ur shape is:')
        Ur = U[:, :r]
        print(Ur.shape)
        print('Sr shape is:')
        Sr = S[:r, :r]
        print(Sr.shape)
        print('Vr shape is:')
        Vr = V[:r, :]
        print(Vr.shape)

        print('Calculating Ar....')
        Ar = Ur.dot(Sr).dot(Vr)
        print('Shape of Ar is:')
        print(Ar.shape)

        print()

        print('Calculating inside mult of q.T with Ar....')
        insideMultQueryPerDoc = np.dot(q.T, Ar)
        print('Shape of insideMultQueryPerDoc is:')
        print(insideMultQueryPerDoc.shape)

        print()

        print('Calculating norm of Ar....')
        normAkPerDoc = np.sqrt(np.sum(Ar*Ar, axis=0))
        print('Shape of normAkPerDoc is:')
        print(normAkPerDoc.shape)

        print('Calculating norm of q....')
        normqPerQuery = np.sqrt(np.sum(q*q, axis=0))
        print('Shape of normqPerQuery is:')
        print(normqPerQuery.shape)

        print('Calculating norm of query x doc....')
        normQueryPerDoc = normAkPerDoc * normqPerQuery[:, np.newaxis]
        print('Shape of normQueryPerDoc is:')
        print(normQueryPerDoc.shape)

        print('Calculating simularity of query x doc....')
        simQueryPerDoc = insideMultQueryPerDoc/normQueryPerDoc
        print('Shape of simQueryPerDoc is:')
        print(simQueryPerDoc.shape)

        print()

        listWithallQueries = []

        print('For every query create a list with tuples(id, simScore) and sort it by score ( Take every row from array "simQueryPerDoc" which contains scores of docs )')
        for i in range(simQueryPerDoc.shape[0]):
            count = 1
            listWithTurplesOfDocs = []
            simQi = simQueryPerDoc[i,:]
            for element in simQi:
                new_tuple = (count, element)
                listWithTurplesOfDocs.append(new_tuple)
                count += 1
            listWithTurplesOfDocs.sort(key=lambda x:x[1])
            listWithallQueries.append(listWithTurplesOfDocs)

        print()

        kList = [20,30,50]
        for k in kList:
            skipQuery = [34, 35, 41, 46, 47, 50, 51, 52, 53, 54, 55, 56]
            fileNameWithPath = 'trec_eval/results_k' + str(k) + '_r' + str(r) + '_lsi.test'
            print('Create file with path {} and start writing....'.format(fileNameWithPath))
            file3 = open(fileNameWithPath, 'w')
            queryCounter = 1
            for list in listWithallQueries:
                if not(queryCounter in skipQuery):
                    for x in reversed(list[(len(list)-k):len(list)]):
                        if queryCounter < 10:
                            if len(str(x[0])) == 1:
                                file3.write('0{} 0 000{} 0 {} LSI'.format(queryCounter,x[0],x[1]))
                            elif len(str(x[0])) == 2:
                                file3.write('0{} 0 00{} 0 {} LSI'.format(queryCounter,x[0],x[1]) )
                            elif len(str(x[0])) == 3:
                                file3.write('0{} 0 0{} 0 {} LSI'.format(queryCounter,x[0],x[1]) )
                            else:
                                file3.write('0{} 0 {} 0 {} LSI'.format(queryCounter,x[0],x[1]) )
                        else:
                            if len(str(x[0])) == 1:
                                file3.write('{} 0 000{} 0 {} LSI'.format(queryCounter,x[0],x[1]) )
                            elif len(str(x[0])) == 2:
                                file3.write('{} 0 00{} 0 {} LSI'.format(queryCounter,x[0],x[1]) )
                            elif len(str(x[0])) == 3:
                                file3.write('{} 0 0{} 0 {} LSI'.format(queryCounter,x[0],x[1]) )
                            else:
                                file3.write('{} 0 {} 0 {} LSI'.format(queryCounter,x[0],x[1]) )
                        file3.write('\n')   
                queryCounter += 1
            file3.close()
            print('Writing done!')
            print()