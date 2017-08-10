import random
import csv


from keras.preprocessing.text import Tokenizer

def read_csv(file_path):

    rows = []

    with open(file_path, "r") as csvfile:
        file = csvfile

        for line in file:
            line = line.replace("\xa0", " ")
            rows.append(line.split(","))

    return rows

def build_tokeniser(sentences):
    t = Tokenizer()
    t.fit_on_texts(sentences)
    return t

def get_category_indeces(category_names):
    category_dict = {}

    indexes = []

    highest_index = -1

    for name in category_names:

        if name not in category_dict:
            highest_index += 1
            category_dict[name] = highest_index

        indexes.append(category_dict[name])

    return indexes
