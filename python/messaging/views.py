from django.shortcuts import render
from tracingdemo.tracing.servicebus import send_messages

# Create your views here.
from django.http import HttpResponse


def eventhub(request):
    return HttpResponse("Hello, world.")

def servicebus(request, message):
    txt = send_messages(message)
    return HttpResponse(txt)