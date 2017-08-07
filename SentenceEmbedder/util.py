import random
import csv


from keras.preprocessing.text import Tokenizer


def read_csv(file_path):

    rows = []

    with open(file_path, "r") as csvfile:
        file = csvfile

        for line in file:
            line = line.replace("\\xa0", " ")
            rows.append(line.split(","))

    return rows


def one_hot_all_sentences(sentences):

    t = Tokenizer(lower=True)

    t.fit_on_texts(sentences)

    print(t.word_index)

    return t.texts_to_sequences(sentences)
